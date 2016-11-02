# Realm在Android项目中的使用
[博客地址](http://shenhuniurou.com/2016/11/02/realm-for-android)


## Introduction

Realm官方文档在Introduction是这样说的：The Realm Mobile Database works on all major mobile platforms to provide offline-first functionality & data persistence through an easy-to-use API. When paired with the Realm Mobile Platform, all your data is automatically synced, with no work from you.Realm是一款轻量级的本地数据库，相比Sqlite更加简单易用，也同样支持跨平台，目前移动端支持的平台有Java、Objective-C、React-Native、Swift和Xamarin，而且也是开源的。这里只介绍Java的使用方法。Realm Java目前Latest version是2.1.1，Java版的源码地址是[https://github.com/realm/realm-java](https://github.com/realm/realm-java)。

## Gifs

<img src="http://offfjcibp.bkt.clouddn.com/realmdemo.gif" width="30%" />

## Prerequisites

- We do not support Java outside of Android at the moment.(目前不支持Java以外版本的android)
- Android Studio >= 1.5.1(Android Studio版本需要1.5.1或更高)
- A recent version of the Android SDK.(最近一个版本的SDK)
- JDK version >=7.(JDK版本要求7或以上)
- We support all Android versions since API Level 9 (Android 2.3 Gingerbread & above).(Realm支持Android 2.3以上的所有版本)

## Installation

Realm是以Gradle plugin插件形式安装的。

**第一步：**在project中的build.gradle中添加class path

```java
buildscript {

    repositories {
        jcenter()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.0'
        classpath "io.realm:realm-gradle-plugin:2.1.1"
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }

}
```

**第二步：**在moudle中的build.gradle中添加apply plugin: 'realm-android'

这样就将Realm集成到我们的项目中来了，下面就来使用它吧。

## Configuration

RealmConfiguration对象是用来控制Realm的创建。

默认配置：
```java
RealmConfiguration config = new RealmConfiguration.Builder().build();
```

以上的默认配置会在`Context.getFilesDir()`路径下生成一个`default.realm`文件。

自定义配置：
```java
// The RealmConfiguration is created using the builder pattern.
// The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
RealmConfiguration config = new RealmConfiguration.Builder()
  .name("myrealm.realm")
  .encryptionKey(getKey())
  .schemaVersion(42)
  .modules(new MySchemaModule())
  .migration(new MyMigration())
  .build();
// Use the config
Realm realm = Realm.getInstance(config);
```

在项目中可以自定义多个RealmConfiguration。这样就可以方便我们独立的控制数据库的版本规范和路径。

```java
RealmConfiguration myConfig = new RealmConfiguration.Builder()
  .name("myrealm.realm")
  .schemaVersion(2)
  .modules(new MyCustomSchema())
  .build();

RealmConfiguration otherConfig = new RealmConfiguration.Builder()
  .name("otherrealm.realm")
  .schemaVersion(5)
  .modules(new MyOtherSchema())
  .build();

Realm myRealm = Realm.getInstance(myConfig);
Realm otherRealm = Realm.getInstance(otherConfig);
```

## Initialization

首先需要自定义一个Application，在onCreate方法中初始化Realm并配置好RealmConfiguration

```java
@Override
public void onCreate() {
	super.onCreate();

	//初始化realm
	Realm.init(this);
	RealmConfiguration config = new RealmConfiguration.Builder().build();
	Realm.setDefaultConfiguration(config);
}
```

## Usage

**字段类型**

Realm支持的字段类型有boolean,byte,short,int,long,float,double,String,Date和byte[]，但是“The integer types(byte
,short,int, and long) are all mapped to the same type (long actually) within Realm.”就是说在Realm中byte、int、short、long这四种类型会被自动映射成long类型，Boolean,Byte,Short,Integer,Long,Float和Double也可以使用，但是用这种类型的话值有可能会被设置成null。

**注解**

@Required用来告诉Realm要强制检查字段不允许空值，而且只有Boolean、Byte、Short、Integer、Long、Float、Double、String、byte[]和Date类型的字段才使用Required。

@Ignore表示忽略该字段，不会被存储到本地。“Ignored fields are useful if your input contains more fields than your model, and you don’t wish to have many special cases for handling these unused data fields.”

@Index给字段增加一个查询索引，它会使插入操作变慢数据文件变大但是查询速度会变的更快。所以这个注解只有在优化读取性能时才推荐使用，支持的字段类型有String、byte、short、int、long、boolean和Date。

@PrimaryKey表示主键，能使用这个注解的字段类型只能是string (String) or integer (byte,short, int, or long) 和他们的封装类(Byte, Short, Integer, and Long)，而且只能有一个字段使用，设置了PrimaryKey注解就意味着自动设置了@Index注解，使用了该注解之后可以使用copyToRealmOrUpdate()方法，通过主键查询它的对象，如果查询到了，则更新它，否则新建一个对象来代替。使用了该注解之后，创建和更新数据将会慢一点，查询数据会快一点。“Creating and updating object will be a little slower while querying is expected to be a bit faster.”

**注意：“When calling Realm.createObject(), it will return a new object with all fields set to the default value. In this case, there might be a conflict with an existing object whose primary key field is the default value. To avoid this, it is suggested to create an unmanaged object, set values of the fields, and then copy it to Realm by copyToRealm() method.”如果使用了PrimaryKey注解，推荐使用copyToRealm来创建对象，因为createObject这种方式可能会引起主键冲突。**


**创建对象**

```java
//方式一
realm.beginTransaction();
User user = realm.createObject(User.class); // Create a new object
user.setName("John");
user.setEmail("john@corporation.com");
realm.commitTransaction();

//方式二
User user = new User("John");
user.setEmail("john@corporation.com");

// Copy the object to Realm. Any further changes must happen on realmUser
realm.beginTransaction();
User realmUser = realm.copyToRealm(user);
realm.commitTransaction();
```

创建对象可以直接使用createObject方法，也可以用copyToRealm方法，两种方式都必需要求创建的对象Model继承自RealmObject，不同的是createObject是直接在Realm中创建了一个对象，而copyToRealm是先创建一个对象，然后将其复制一份到Realm中，所以“When using realm.copyToRealm(), it is important to remember that only the returned object is managed by Realm, so any further changes to the original object will not be persisted.”也就是说这种方式创建的对象他的原始对象的任何变化都不会被存储。


**事务块**

为了代替手动开启事务关闭事务取消事务这一系列操作，我们可以使用事务块，它可以自动处理事务的开始提交和取消。

```java
realm.executeTransaction(new Realm.Transaction() {
	@Override
	public void execute(Realm realm) {
		User user = realm.createObject(User.class);
		user.setName("John");
		user.setEmail("john@corporation.com");
	}
});
```

**异步事务块**

当事务被其他事务阻塞时，为了避免阻塞UI线程，它可以到后台线程去做所有的写操作，如果使用异步事务，Realm会将这个事务运行在后台线程，当事务处理完毕后返回到UI线程。OnSuccess和OnError这两个回调方法都是可选的，但是如果写了，在事务处理成功或失败后他们就一定会被调用。

```java
realm.executeTransactionAsync(
	new Realm.Transaction() {
		@Override
		public void execute(Realm bgRealm) {
			User user = bgRealm.createObject(User.class);
			user.setName("John");
			user.setEmail("john@corporation.com");
		}
	}, 
	new Realm.Transaction.OnSuccess() {
		@Override
		public void onSuccess() {
			// Transaction was a success.
		}
	}, 
	new Realm.Transaction.OnError() {
		@Override
		public void onError(Throwable error) {
			// Transaction failed and was automatically canceled.
		}
	});
```

*注意：使用异步操作时，execute(Realm bgRealm) 方法中的参数bgRealm是后台线程创建的，而Realm规定Realm对象只能在创建它的线程里被访问，所以，在UI线程中创建的Realm对象是不可以在execute方法中使用的，否则会抛异常：Caused by: java.lang.IllegalStateException: Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.*


**增加和异步增加**

```java
@OnClick(R.id.tvAdd)
void add() {
	Realm mRealm = Realm.getDefaultInstance();

	// Create a new object
	mRealm.beginTransaction();
	for (int i = 0; i < 100; i++) {
		Person person = mRealm.createObject(Person.class, i);
		person.setName("shenhuniurou---" + i);
		person.setEmail("shenhuniurou@gmail.com---" + i);
	}
	mRealm.commitTransaction();
	Toast.makeText(this, "成功添加数据", Toast.LENGTH_SHORT).show();
}

@OnClick(R.id.tvAsyncAdd)
void asyncAdd() {
	persons = new ArrayList<>();
	for (int i = 0; i < 100; i++) {
		Person person = new Person();
		person.setId(i + 100);
		person.setName("神户牛肉---" + i);
		person.setEmail("shenhuniurou@gmail.com---" + i);
		persons.add(person);
	}
	addPerson(persons);
}

private void addPerson(final List<Person> persons) {
	Realm mRealm = Realm.getDefaultInstance();

	addTask = mRealm.executeTransactionAsync(
		new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				for (Person person: persons) {
					realm.copyToRealm(person);
				}
			}
		}, 
		new Realm.Transaction.OnSuccess() {
			@Override
			public void onSuccess() {
				Toast.makeText(MainActivity.this, "增加数据成功", Toast.LENGTH_SHORT).show();
			}
		}, 
		new Realm.Transaction.OnError() {
			@Override
			public void onError(Throwable error) {
				String message = error.getMessage();
				Toast.makeText(MainActivity.this, "增加数据失败---" + message, Toast.LENGTH_SHORT).show();
			}
		});

}
```

另外使用异步操作时，如果在操作未完成前推出界面，不要忘记取消task，否则异步任务回调更新UI时可能会引起app崩溃。“if you are quitting the Activity/Fragment before the transaction is completed. Forgetting to cancel a transaction can crash the app if the callback updates the UI.”

```java
public void onStop () {
    if (transaction != null && !transaction.isCancelled()) {
        transaction.cancel();
    }
}
```

**查询和异步查询**

```java
//查询所有
public List<Person> queryAll() {
	Realm mRealm = Realm.getDefaultInstance();
	RealmResults<Person> persons = mRealm.where(Person.class).findAll();
	// 排序
	persons = persons.sort("id");
	return mRealm.copyFromRealm(persons);
}

//条件查询
public Person queryByCondition(String id) {
	Realm mRealm = Realm.getDefaultInstance();
	Person person = mRealm.where(Person.class).equalTo("id", id).findFirst();
	return person;
}

//异步查询所有
public void queryAll() {
	final Realm mRealm = Realm.getDefaultInstance();
	persons = mRealm.where(Person.class).findAllAsync();
	persons.addChangeListener(new RealmChangeListener<RealmResults<Person>>() {
		@Override
		public void onChange(RealmResults<Person> element) {
			element = element.sort("id");
			List<Person> personList = mRealm.copyFromRealm(element);
			mAdapter.addAll(personList);
			recyclerView.scrollToPosition(personList.size() - 1);
			persons.removeChangeListeners();
		}
	});
}
```

常用的几个条件查询：

- between(),greaterThan(),lessThan(),greaterThanOrEqualTo() & lessThanOrEqualTo()

- equalTo() & notEqualTo()

- contains(), beginsWith() & endsWith()

- isNull() & isNotNull()

- isEmpty() & isNotEmpty()

还可以组合使用

```java
RealmResults<Person> r = realm.where(Person.class)
                            .greaterThan("age", 10)  //implicit AND
                            .beginGroup()
                                .equalTo("name", "Peter")
                                .or()
                                .contains("name", "Jo")
                            .endGroup()
                            .findAll();
```

其他查询，sum，min，max，average只支持整型数据字段

```java
RealmResults<User> results = realm.where(User.class).findAll();
long sum = results.sum("age").longValue();
long min = results.min("age").longValue();
long max = results.max("age").longValue();
double average = results.average("age");

long matches = results.size();
```

*注意:Not all conditions are applicable for all data types,并不是所有的条件都适用于所有数据类型。*

**更新和异步更新**

```java
Person item = mAdapter.getItem(position);
Realm  mRealm = Realm.getDefaultInstance();
Person person = mRealm.where(Person.class).equalTo("id", item.getId()).findFirst();
mRealm.executeTransaction(new Realm.Transaction() {
	@Override
	public void execute(Realm realm) {
		person.setName("神户牛肉" + position);
		item.setName(person.getName());
		mAdapter.notifyItemChanged(position);
	}
});

//异步更新
Realm mRealm = Realm.getDefaultInstance();
updateTask = mRealm.executeTransactionAsync(
	new Realm.Transaction() {
		@Override
		public void execute(Realm realm) {
			Person person = realm.where(Person.class).equalTo("id", item.getId()).findFirst();
			person.setName("神户牛肉" + position);
			item.setName(person.getName());
		}
	}, 
	new Realm.Transaction.OnSuccess() {
		@Override
		public void onSuccess() {
			mAdapter.notifyItemChanged(position);
			Toast.makeText(PersonListAsyncActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
		}
	}, 
	new Realm.Transaction.OnError() {
		@Override
		public void onError(Throwable error) {
			Toast.makeText(PersonListAsyncActivity.this, "更新失败---" + error.getMessage(), Toast.LENGTH_SHORT).show();
		}
	});

//取消task
@Override
protected void onStop() {
	super.onStop();

	if (updateTask != null && !updateTask.isCancelled()) {
		updateTask.cancel();
	}
}
```

**删除和异步删除**

```java
Person item = mAdapter.getItem(position);
Realm  mRealm = Realm.getDefaultInstance();
Person person = mRealm.where(Person.class).equalTo("id", item.getId()).findFirst();
mRealm.executeTransaction(new Realm.Transaction() {
	@Override
	public void execute(Realm realm) {
		person.deleteFromRealm();
	}
});

//异步删除
Realm mRealm = Realm.getDefaultInstance();
deleteTask = mRealm.executeTransactionAsync(
	new Realm.Transaction() {
		@Override
		public void execute(Realm realm) {
			Person person = realm.where(Person.class).equalTo("id", item.getId()).findFirst();
			person.deleteFromRealm();
		}
	}, 
	new Realm.Transaction.OnSuccess() {
		@Override
		public void onSuccess() {
			mAdapter.remove(position);
			Toast.makeText(PersonListAsyncActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
		}
	}, 
	new Realm.Transaction.OnError() {
		@Override
		public void onError(Throwable error) {
			Toast.makeText(PersonListAsyncActivity.this, "删除失败---" + error.getMessage(), Toast.LENGTH_SHORT).show();
		}
	});

//取消task
@Override
protected void onStop() {
	super.onStop();
	
	if (deleteTask != null && !deleteTask.isCancelled()) {
		deleteTask.cancel();
	}
}
```

就简单地介绍这么多吧，基本使用就是这些，还有更多细节的东西还是要去看[官方文档](https://realm.io/docs/java/latest/)。使用场景比如搜索的历史记录，数据缓存等等。接下来准备学习下Realm在Objective-C平台下的使用。