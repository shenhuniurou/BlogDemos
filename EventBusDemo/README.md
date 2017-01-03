## EventBus的基本使用和源码解析
[博客地址](http://shenhuniurou.com/2016/11/06/android-event-bus-source-parsing)

最近在项目中使用了EventBus(3.0)，觉得非常好用，于是就看了一些关于EventBus源码分析的文章，现在记录下它的基本使用方法和看源码过程中分析的一些问题。[EventBus源码地址](https://github.com/greenrobot/EventBus)


![11212.gif](http://upload-images.jianshu.io/upload_images/1159224-bee2ae64e82d2340.gif?imageMogr2/auto-orient/strip)


## EventBus是什么？

EventBus是一个Android事件发布和订阅的框架，通过解耦发布者和订阅者来简化Android事件传递。

## EventBus是干什么的？


![EventBus-Publish-Subscribe.png](http://upload-images.jianshu.io/upload_images/1159224-bb2f6dfa7493b635.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


事件传递既可以用于Android四大组件间通讯，也可以用于异步线程和主线程间通讯等。传统的事件传递方式包括：`Handler`、`BroadcastReceiver`、`Interface回调`，而EventBus就是用来传递事件的，相比传统的方式，它代码简洁，使用简单，并将事件发布和订阅充分解耦。

## 基本使用方法

**添加依赖库**

```java
//在app下的build.gradle中添加下面的依赖
compile 'org.greenrobot:eventbus:3.0.0'
```

**注册订阅事件**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	EventBus.getDefault().register(this);
}
```

**取消注册订阅事件**

```java
@Override
protected void onDestroy() {
	super.onDestroy();
	EventBus.getDefault().unregister(this);
}
```

**发布事件**
```java
EventBus.getDefault().post(Object obj);
```

**订阅事件**
```java
@Subscribe(threadMode = ThreadMode.MAIN)
public void onEvent(Object obj) {
}
```

看吧，使用起来还是挺简单的，订阅者真正需要处理逻辑的就是在有注解@subscribe的方法`onEvent(Object obj)`中去按照我们自己的需求去处理，当然这里方法名不一定是`onEvent`了，我是受2.4版本影响，习惯了把方法名写成`onEvent`，在3.0版本中可以随意写，但是参数的`class`类型必须要跟发布者`post`的参数的`class`类型一致。

**使用场景**

比如有一个列表页面，每个item上都有评论数，点赞数和点赞状态，点进去到详情页，用户可以进行一些操作，评论，点赞，这时候详细页的评论数点赞数和点赞状态我们可以立即更新，但是当我们回到列表页面的时候，也需要更新UI啊，之前也说了，传统的事件传递方式有Handler，BroadcastReceiver，以及Interface回调等，如果不使用EventBus的话，我会考虑使用广播的方式或者用`startActivityForResult`来打开详细页，等到从详细页返回到列表页时，将操作后的对象传递回来，然后在`onActivityResult`方法中去处理更新UI。但是这种方法的弊端在于只有在返回到上一个界面时才会将事件传递回来，不能达到立即更新的目的。

**线程模式**

上面说过，只有使用注解`@Subscribe(threadMode = ThreadMode.MAIN)`标明的方法才是订阅者需要处理逻辑的地方。那这个`threadMode `是什么呢？它其实是指定订阅者处理事件的线程，EventBus总共有四种线程模式，分别是：

- ThreadMode.MAIN：表示无论事件是在哪个线程发布出来的，该事件订阅方法onEvent都会在UI线程中执行，这个在Android中是非常有用的，因为在Android中只能在UI线程中更新UI，所有在此模式下的方法是不能执行耗时操作的。

- ThreadMode.POSTING：表示事件在哪个线程中发布出来的，事件订阅函数onEvent就会在这个线程中运行，也就是说发布事件和接收事件在同一个线程。使用这个方法时，在onEvent方法中不能执行耗时操作，如果执行耗时操作容易导致事件分发延迟。

- ThreadMode.BACKGROUND：表示如果事件在UI线程中发布出来的，那么订阅函数onEvent就会在子线程中运行，如果事件本来就是在子线程中发布出来的，那么订阅函数直接在该子线程中执行。

- ThreadMode.ASYNC：使用这个模式的订阅函数，那么无论事件在哪个线程发布，都会创建新的子线程来执行订阅函数。

> ASYNC相比前三者不同的地方是可以处理耗时的操作，其采用了线程池，且是一个异步执行的过程，即事件的订阅者可以立即得到执行。虽然BACKGROUND也采用了线程池，但它每次只能执行一个任务，就是不会异步执行。所以一般是更新UI的事件，就使用ThreadMode.MAIN，需要请求网络的事件就使用ThreadMode.AYSNC,。

```java
final class BackgroundPoster implements Runnable {

    private final PendingPostQueue queue;
    private final EventBus eventBus;

    private volatile boolean executorRunning;

    BackgroundPoster(EventBus eventBus) {
        this.eventBus = eventBus;
        queue = new PendingPostQueue();
    }

    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this) {
            queue.enqueue(pendingPost);
            if (!executorRunning) {
                executorRunning = true;
                eventBus.getExecutorService().execute(this);
            }
        }
    }

    @Override
    public void run() {
        try {
            try {
                while (true) {
                    PendingPost pendingPost = queue.poll(1000);
                    if (pendingPost == null) {
                        synchronized (this) {
                            // Check again, this time in synchronized
                            pendingPost = queue.poll();
                            if (pendingPost == null) {
                                executorRunning = false;
                                return;
                            }
                        }
                    }
                    eventBus.invokeSubscriber(pendingPost);
                }
            } catch (InterruptedException e) {
                Log.w("Event", Thread.currentThread().getName() + " was interruppted", e);
            }
        } finally {
            executorRunning = false;
        }
    }

}
```

backgroundPoster通过enqueue方法，将当前的订阅者添加至队列PendingPostQueue
中，是否立即执行，则需要判断当前队列是否还有正在执行的任务，若没有的话，则立即执行，若还有执行任务的话，则只进行队列的添加。这样，保证了后台任务永远只会在一个线程执行。

```java
class AsyncPoster implements Runnable {

    private final PendingPostQueue queue;
    private final EventBus eventBus;

    AsyncPoster(EventBus eventBus) {
        this.eventBus = eventBus;
        queue = new PendingPostQueue();
    }

    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        queue.enqueue(pendingPost);
        eventBus.getExecutorService().execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        if(pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        eventBus.invokeSubscriber(pendingPost);
    }

}
```

AsyncPoster是直接通过线程池调用执行，相比BackgroundPoster执行来说，则没有等待的过程。

```java
final class HandlerPoster extends Handler {

    private final PendingPostQueue queue;
    private final int maxMillisInsideHandleMessage;
    private final EventBus eventBus;
    private boolean handlerActive;

    HandlerPoster(EventBus eventBus, Looper looper, int maxMillisInsideHandleMessage) {
        super(looper);
        this.eventBus = eventBus;
        this.maxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
        queue = new PendingPostQueue();
    }

    void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this) {
            queue.enqueue(pendingPost);
            if (!handlerActive) {
                handlerActive = true;
                if (!sendMessage(obtainMessage())) {
                    throw new EventBusException("Could not send handler message");
                }
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        boolean rescheduled = false;
        try {
            long started = SystemClock.uptimeMillis();
            while (true) {
                PendingPost pendingPost = queue.poll();
                if (pendingPost == null) {
                    synchronized (this) {
                        // Check again, this time in synchronized
                        pendingPost = queue.poll();
                        if (pendingPost == null) {
                            handlerActive = false;
                            return;
                        }
                    }
                }
                eventBus.invokeSubscriber(pendingPost);
                long timeInMethod = SystemClock.uptimeMillis() - started;
                if (timeInMethod >= maxMillisInsideHandleMessage) {
                    if (!sendMessage(obtainMessage())) {
                        throw new EventBusException("Could not send handler message");
                    }
                    rescheduled = true;
                    return;
                }
            }
        } finally {
            handlerActive = rescheduled;
        }
    }
}
```

HandlerPoster是我们再熟悉不过的Handler消息机制了，它会将每个订阅事件传递到UI线程中进行处理。这里就不多说了。

## 源码分析

**EventBus的事件总线**

在EventBus中，真正的订阅对象是`SubscriberMethod`，包含了相应的`Method类`，以及事件参数类型`Class<?> eventType`，其他就是线程，优先级，是否`Sticky`信息。

```java
public class SubscriberMethod {
    final Method method;
    final ThreadMode threadMode;
    final Class<?> eventType;
    final int priority;
    final boolean sticky;
}
```

> Sticky=true表示等到回到事件订阅的界面时才开始传递消，而不是一post就开始传递，什么时候使用sticy呢？当你希望你的事件不被马上处理的时候。

事件总线，一般都对应着一个集合，这个集合中的对象就是订阅的事件，而EventBus中使用的是：

```java
private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;

subscriptionsByEventType = new HashMap<>();
```

这里的Map集合采用的是一个HashMap集合，map的key对应就是之前`SubscriberMethod`中的`eventType`, value则对应着一个线程安全的List，List中存放的是包含订阅对象`Object`及相应订阅方法`SubscriberMethod`的`Subscription`类：

```java
final class Subscription {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;
}
```

所以，EventBus内部是用了一个线程安全的List集合来维持所有的订阅者，即事件总线集合。而注册订阅和取消注册订阅就是对这个List集合进行增删的过程。

**注册订阅和取消注册订阅**

```java
/**
 * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they
 * are no longer interested in receiving events.
 * <p/>
 * Subscribers have event handling methods that must be annotated by {@link Subscribe}.
 * The {@link Subscribe} annotation also allows configuration like {@link
 * ThreadMode} and priority.
 */
public void register(Object subscriber) {
	Class<?> subscriberClass = subscriber.getClass();
	List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
	synchronized (this) {
		for (SubscriberMethod subscriberMethod : subscriberMethods) {
			subscribe(subscriber, subscriberMethod);
		}
	}
}
```

首先EventBus会根据订阅类的Class去这个类中查找处理订阅事件的方法，`SubscriberMethodFinder`这个类看名字我们就知道它是干什么用的了，等会在细说。下面我们看看`subscribe`方法：

```java
// Must be called in synchronized block
private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
	Class<?> eventType = subscriberMethod.eventType;
	Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
	CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
	if (subscriptions == null) {
		subscriptions = new CopyOnWriteArrayList<>();
		subscriptionsByEventType.put(eventType, subscriptions);
	} else {
		if (subscriptions.contains(newSubscription)) {
			throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
					+ eventType);
		}
	}

	int size = subscriptions.size();
	for (int i = 0; i <= size; i++) {
		if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
			subscriptions.add(i, newSubscription);
			break;
		}
	}

	List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
	if (subscribedEvents == null) {
		subscribedEvents = new ArrayList<>();
		typesBySubscriber.put(subscriber, subscribedEvents);
	}
	subscribedEvents.add(eventType);

	if (subscriberMethod.sticky) {
		if (eventInheritance) {
			// Existing sticky events of all subclasses of eventType have to be considered.
			// Note: Iterating over all events may be inefficient with lots of sticky events,
			// thus data structure should be changed to allow a more efficient lookup
			// (e.g. an additional map storing sub classes of super classes: Class -> List<Class>).
			Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
			for (Map.Entry<Class<?>, Object> entry : entries) {
				Class<?> candidateEventType = entry.getKey();
				if (eventType.isAssignableFrom(candidateEventType)) {
					Object stickyEvent = entry.getValue();
					checkPostStickyEventToSubscription(newSubscription, stickyEvent);
				}
			}
		} else {
			Object stickyEvent = stickyEvents.get(eventType);
			checkPostStickyEventToSubscription(newSubscription, stickyEvent);
		}
	}
}
```

从这个方法我们可以看到每一个`eventType` 都对应一个`CopyOnWriteArrayList<Subscription>`，不过这样读源码太不好理解了，结合我Demo中的例子来说，`MainActivity`中的`subscriberMethod`就是`onEvent(NewsModel newsModel)`方法，所以`eventType`就是`NewsModel`类，而`subscriber`就是MainActivity的实例，他俩组合成了一个订阅者`Subscription`，然后把`eventType`作为key，订阅了`NewsModel`这类事件的订阅者的集合作为value存储在一个HashMap中，最后根据优先级`priority `将`newSubscription `这个最新的订阅者添加到订阅列表中。也就是说在一个Activity页面类，我们可以有多个SubscriberMethod，就像下面这样：

```java
@Subscribe(threadMode = ThreadMode.MAIN)
public void onEvent(NewsModel newsModel) {
	newsList.set(clickPosition, newsModel);
	mAdapter.notifyItemChanged(clickPosition);
}

@Subscribe(threadMode = ThreadMode.BACKGROUND)
public void onEvent1(User user) {
	
}

@Subscribe(threadMode = ThreadMode.ASYNC)
public void onEvent2(Person person) {
	
}
```

取消注册订阅则是将该事件类型的订阅者从事件总线集合中移除：

```java
/** Unregisters the given subscriber from all event classes. */
public synchronized void unregister(Object subscriber) {
	List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
	if (subscribedTypes != null) {
		for (Class<?> eventType : subscribedTypes) {
			unsubscribeByEventType(subscriber, eventType);
		}
		typesBySubscriber.remove(subscriber);
	} else {
		Log.w(TAG, "Subscriber to unregister was not registered before: " + subscriber.getClass());
	}
}

/** Only updates subscriptionsByEventType, not typesBySubscriber! Caller must update typesBySubscriber. */
private void unsubscribeByEventType(Object subscriber, Class<?> eventType) {
	List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
	if (subscriptions != null) {
		int size = subscriptions.size();
		for (int i = 0; i < size; i++) {
			Subscription subscription = subscriptions.get(i);
			if (subscription.subscriber == subscriber) {
				subscription.active = false;
				subscriptions.remove(i);
				i--;
				size--;
			}
		}
	}
}
```

**事件消费和发布事件**

这里通过EventBus的post(Object event)方法，进行事件的发出。紧接着EventBus的总线List中找出订阅了这个event的方法Subscription，然后根据method指定的不同线程信息，将这个方法的调用，放置在相应线程中调用，看看EventBus中的`post`方法：

```java
/** Posts the given event to the event bus. */
public void post(Object event) {
	PostingThreadState postingState = currentPostingThreadState.get();
	List<Object> eventQueue = postingState.eventQueue;
	eventQueue.add(event);

	if (!postingState.isPosting) {
		postingState.isMainThread = Looper.getMainLooper() == Looper.myLooper();
		postingState.isPosting = true;
		if (postingState.canceled) {
			throw new EventBusException("Internal error. Abort state was not reset");
		}
		try {
			while (!eventQueue.isEmpty()) {
				postSingleEvent(eventQueue.remove(0), postingState);
			}
		} finally {
			postingState.isPosting = false;
			postingState.isMainThread = false;
		}
	}
}
```

顺着`postSingleEvent`方法看下来，我们会发现最终将会调用方法`postToSubscription`将发布事件传递到订阅者这里来。

```java
private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
	switch (subscription.subscriberMethod.threadMode) {
		case POSTING:
			invokeSubscriber(subscription, event);
			break;
		case MAIN:
			if (isMainThread) {
				invokeSubscriber(subscription, event);
			} else {
				mainThreadPoster.enqueue(subscription, event);
			}
			break;
		case BACKGROUND:
			if (isMainThread) {
				backgroundPoster.enqueue(subscription, event);
			} else {
				invokeSubscriber(subscription, event);
			}
			break;
		case ASYNC:
			asyncPoster.enqueue(subscription, event);
			break;
		default:
			throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
	}
}
```

```java
void invokeSubscriber(Subscription subscription, Object event) {
	try {
		subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
	} catch (InvocationTargetException e) {
		handleSubscriberException(subscription, event, e.getCause());
	} catch (IllegalAccessException e) {
		throw new IllegalStateException("Unexpected exception", e);
	}
}
```

然后通过反射调用订阅者的消费该事件的方法。也许当你看上面`postToSubscription`方法时会发现`threadMode`是`MAIN`、`BACKGROUND`或者`ASYNC`时，都有用各自县城的poster调用`enqueue`方法，其实仔细一层层看下去你会发现到最后还是调用了`invokeSubscriber`方法。所以，最终的消费方法调用就是这行代码：

```java
subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
```

订阅者已经被封装地非常完美，这样，我们在使用不同的线程调度策略就很简单了，随意指定一个`ThreadMode`，即可在指定线程中调用。

看到这里，也许你会发现有个很大的疑问：`SubscriberMethod`信息是怎么生成的？其实就是加了注解`@Subscribe`的方法。我们可以通过在运行时采用反射的方法，获取相应添加了注解的方法，再封装成为`SubscriberMethod`。也就是前面我提到的`SubscriberMethodFinder`类来获取消费事件方法。看源码，真正在运行时利用反射去查找`SubscriberMethod`的代码：

```java
private void findUsingReflectionInSingleClass(FindState findState) {
	Method[] methods;
	try {
		// This is faster than getMethods, especially when subscribers are fat classes like Activities
		methods = findState.clazz.getDeclaredMethods();
	} catch (Throwable th) {
		// Workaround for java.lang.NoClassDefFoundError, see https://github.com/greenrobot/EventBus/issues/149
		methods = findState.clazz.getMethods();
		findState.skipSuperClasses = true;
	}
	for (Method method : methods) {
		int modifiers = method.getModifiers();
		if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 1) {
				Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
				if (subscribeAnnotation != null) {
					Class<?> eventType = parameterTypes[0];
					if (findState.checkAdd(method, eventType)) {
						ThreadMode threadMode = subscribeAnnotation.threadMode();
						findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,
								subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
					}
				}
			} else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
				String methodName = method.getDeclaringClass().getName() + "." + method.getName();
				throw new EventBusException("@Subscribe method " + methodName +
						"must have exactly 1 parameter but has " + parameterTypes.length);
			}
		} else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
			String methodName = method.getDeclaringClass().getName() + "." + method.getName();
			throw new EventBusException(methodName +
					" is a illegal @Subscribe method: must be public, non-static, and non-abstract");
		}
	}
}
```

对于我们开发者来说，使用反射带来的性能消耗，是必须要考虑到的。在3.0的版本，`EventBus`加入了`apt`处理的逻辑，有个[Subscriber Index](http://greenrobot.org/eventbus/documentation/subscriber-index/)的介绍，主要是通过Apt在编译期根据注解直接生成相应的信息，来避免在运行时通过反射来获取。使用方法如下：

```java
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
 
apply plugin: 'com.neenbedankt.android-apt'
 
dependencies {
    compile 'org.greenrobot:eventbus:3.0.0'
    apt 'org.greenrobot:eventbus-annotation-processor:3.0.1'
}
 
apt {
    arguments {
        eventBusIndex "com.example.myapp.MyEventBusIndex"
    }
}
```


![11211.png](http://upload-images.jianshu.io/upload_images/1159224-70e79ea8ba9b016a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

上面的配置除了第一条是在project的build.gradle中添加，其余的都是在moudle中的build.gradle中添加，具体的大家可以看我的Demo中配置方法。当我们再次编译之后，系统会自动为我们生成`MyEventBusIndex`这个类，然后我们将它设置给EventBus：

```java
EventBus eventBus = EventBus.builder().addIndex(new MyEventBusIndex()).build();
```

或者是这样：

```java
EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
// Now the default instance uses the given index. Use it like this:
EventBus eventBus = EventBus.getDefault();
```

`MyEventBusIndex`这个生成的索引类实际上会实现`SubscriberInfoIndex`这个接口：

```java
public interface SubscriberInfoIndex {
    SubscriberInfo getSubscriberInfo(Class<?> subscriberClass);
}
```

从接口中可以看出，这个Index类只提供了一个方法`getSubsriberInfo`，这个方法需要我们传入订阅者所在的class类，然后获得一个`SubscriberInfo`的类，其类结构如下：

```java
public interface SubscriberInfo {
    Class<?> getSubscriberClass();

    SubscriberMethod[] getSubscriberMethods();

    SubscriberInfo getSuperSubscriberInfo();

    boolean shouldCheckSuperclass();
}
```

从接口中的方法即可获取所需的所有`SubscriberMethod`信息。而这只是一个获取单个类的方法，而apt生成的`MyEventBusIndex`中，会将所有的这些class及`SubscriberInfo`保存在静态变量`hashMap`结构，这样就达到了避免运行期反射获取生成订阅方法的性能问题。而变成另外一个流程：订阅类在注册的时候，直接通过`HashMap`中的订阅类，获取到`SubscriberInfo`，进而获取到所有的`SubscerberMethod`，并封装为`Subscription`，被添加到事件总线中。这样，在引入了apt之后，EventBus的性能问题就得以解决了。

说完了性能问题，还得接着说`SubscriberMethodFinder`这个类，它会根据订阅类class信息，来获取`SubscriberMethod`，EventBus提供了两种方式进行获取：

```java
if (ignoreGeneratedIndex) {
	subscriberMethods = findUsingReflection(subscriberClass);
} else {
	subscriberMethods = findUsingInfo(subscriberClass);
}
```

- 如果不使用生成的索引来查找，就采用`findUsingReflection(subscriberClass)`方法进行反射来获取。
- 使用生成的索引来查找，就采用`findUsingInfo(subscriberClass)`方法在apt中进行查找获取。

而这个`ignoreGeneratedIndex`默认是false的，如果没有在项目中引入apt则将强制使用反射方式查找。

```java
/** Forces the use of reflection even if there's a generated index (default: false). */
public EventBusBuilder ignoreGeneratedIndex(boolean ignoreGeneratedIndex) {
	this.ignoreGeneratedIndex = ignoreGeneratedIndex;
	return this;
}
```

在查找`SubscriberMethod`时，EventBus封装了一个类`FindState`对查找的状态值处理，结构如下：

```java
static class FindState {
	final List<SubscriberMethod> subscriberMethods = new ArrayList<>();
	final Map<Class, Object> anyMethodByEventType = new HashMap<>();
	final Map<String, Class> subscriberClassByMethodKey = new HashMap<>();
	final StringBuilder methodKeyBuilder = new StringBuilder(128);

	Class<?> subscriberClass;
	Class<?> clazz;
	boolean skipSuperClasses;
	SubscriberInfo subscriberInfo;
}
```

其中有订阅类`subscriberClass`，事件对象clazz，以及查找的结果`subscriberMethods`、`subscriberInfo`等，另外，还有一个判断的标志量`skipSuperClasses`，用来标记是否需要进行父类的查找。

无论是哪种查找方法，步骤都是一样的，于是`FindState`将这些通用的步骤封装起来，大致是这四步：

- initForSubscriber(Class<?> subscriberClass) 初始化订阅类
- checkAdd(Method method, Class<?> eventType) 检查方法合法性
- checkAddWithMethodSignature(Method method, Class<?> eventType) 检查方法合法性
- moveToSuperclass() 是否需要查看父类中的订阅方法

除了对查找结果的封装，FindState还使用了缓存池：

```java
private static final int POOL_SIZE = 4;
private static final FindState[] FIND_STATE_POOL = new FindState[POOL_SIZE];

private FindState prepareFindState() {
	synchronized (FIND_STATE_POOL) {
		for (int i = 0; i < POOL_SIZE; i++) {
			FindState state = FIND_STATE_POOL[i];
			if (state != null) {
				FIND_STATE_POOL[i] = null;
				return state;
			}
		}
	}
	return new FindState();
}
```

EventBus指定了FindState的缓存池的大小为4，并使用一维的静态数组，所以这里需要注意线程同步的问题。使用同步代码块从缓存池中取FindState，同样，通过上面两种方式获取`SubscriberMethod`时，也使用同步代码块将FindState放入了缓存池中。

```java
private List<SubscriberMethod> getMethodsAndRelease(FindState findState) {
	List<SubscriberMethod> subscriberMethods = new ArrayList<>(findState.subscriberMethods);
	findState.recycle();
	synchronized (FIND_STATE_POOL) {
		for (int i = 0; i < POOL_SIZE; i++) {
			if (FIND_STATE_POOL[i] == null) {
				FIND_STATE_POOL[i] = findState;
				break;
			}
		}
	}
	return subscriberMethods;
}
```

### 总结

分析了EventBus的源码后，相信你会对它的工作方式更加清晰，简而言之，就是“发布者发布事件，订阅者通过反射的方式根据发布事件的class类型查找SubscriberMethod，然后通过这个类来invoke订阅类中处理对应事件的方法”。它的引入大大简化了开发者需要做的工作，而且将订阅者和发布者完全解耦，so赶紧用起来吧。

[演示的Demo下载地址](https://github.com/shenhuniurou/BlogDemos/tree/master/EventBusDemo)

## 参考文档

- [EventBus Documentation](http://greenrobot.org/eventbus/documentation)
- [EventBus 3.0的用法详解（一）](https://segmentfault.com/a/1190000004279679)
- [EventBus 3.0的用法详解(二)](https://segmentfault.com/a/1190000004314315)
- [浅析EventBus 3.0实现思想](http://alighters.com/blog/2016/05/22/eventbus3-dot-0-analyze/)
- [EventBus后续深入知识点整理](http://www.jianshu.com/p/f8fd67eef9aa)
