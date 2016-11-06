# Matrix的原理及使用方法
[博客地址](http://shenhuniurou.com/2016/11/05/matrix-principle-and-intructions)

## 基本原理

Matrix 是一个3*3的矩阵，最根本的作用就是坐标转换，如图：

<img src="http://offfjcibp.bkt.clouddn.com/QQ%E6%88%AA%E5%9B%BE20161105231828.png" width="20%" />

Matrix的基本变换有4种: 平移(translate)、缩放(scale)、旋转(rotate) 和 错切(skew)。Matrix类有九个静态常量，通过不同的组合方式来控制不同的变换。

```java
public static final int MSCALE_X = 0;   //!< use with getValues/setValues
public static final int MSKEW_X  = 1;   //!< use with getValues/setValues
public static final int MTRANS_X = 2;   //!< use with getValues/setValues
public static final int MSKEW_Y  = 3;   //!< use with getValues/setValues
public static final int MSCALE_Y = 4;   //!< use with getValues/setValues
public static final int MTRANS_Y = 5;   //!< use with getValues/setValues
public static final int MPERSP_0 = 6;   //!< use with getValues/setValues
public static final int MPERSP_1 = 7;   //!< use with getValues/setValues
public static final int MPERSP_2 = 8;   //!< use with getValues/setValues
```

这九个常量是存储在一个一维数组中的，可以通过getValues获取到这个数组，其中：

- MSCALE_X和MSCALE_Y控制缩放变换
- MSKEW_X和MSKEW_Y控制错切变换
- MTRANS_X和MTRANS_Y控制平移变换
- MSCALE_X、MSKEW_X、MSCALE_Y和MSKEW_Y控制旋转变换
- MPERSP_0、MPERSP_1和MPERSP_2则控制透视变换

如图所示：

<img src="http://offfjcibp.bkt.clouddn.com/005Xtdi2jw1f60gwrhlnyj30c008zdgy.jpg" width="30%" />
<img src="http://offfjcibp.bkt.clouddn.com/005Xtdi2jw1f633hvklfnj30c008zdge.jpg" width="30%" />

除平移变换(Translate)外，旋转变换(Rotate)、缩放变换(Scale)和错切变换(Skew)都可以围绕一个中心点来进行，如果不指定，在默认情况下是围绕(0, 0)来进行相应的变换的。


### 平移变换

假定有一个点的坐标是p(x0, y0)，将其移动到p(x, y)，再假定在x轴和y轴方向移动的大小分别为:

```java
dx = x - x0;
dy = y - y0;
可以得到:
x = x0 + dx;
y = y0 + dy;
```

用矩阵表示是这样的：

![](http://offfjcibp.bkt.clouddn.com/png.png)

可以看出平移变换只与第三个和第六个值有关。

### 旋转变换

旋转变换可分为绕坐标原点旋转和绕某一点旋转：

#### 绕坐标原点旋转

假定有一个点p(x0, y0)，它与原点的连线和x轴的角度为α，相对坐标原点顺时针旋转β后到达点p(x, y)，同时假定p点离坐标原点的距离为r，则可以计算得到

```java
x0 = rcosα;
y0 = rsinα;
//根据和差角公式
x = rcos(α + β) = rcosαcosβ - rsinαsinβ = x0cosβ - y0sinβ;
y = rsin(α + β) = rsinαcosβ + rcosαsinβ = x0sinβ + y0cosβ;
```

用矩阵表示是这样的：

![](http://offfjcibp.bkt.clouddn.com/rotate.png)

可以看出旋转变换只与这四个值值有关，而且只与角度β有关。

#### 绕某个点旋转

绕某个点旋转，可以分成3个步骤，即首先将坐标原点移至该点，然后围绕新的坐标原点进行旋转变换，再然后将坐标原点移回到原先的坐标原点。但是四大操作都可以指定中心点，所以，这三个步骤其实用一个方法就可以搞定。

```java
matrix.postRotate(β, xp, yp);
```

### 缩放变换

一个点是不存在什么缩放变换的，但考虑到所有图像都是由点组成，因此，如果图像在x轴和y轴方向分别放大k1和k2倍的话，那么图像中的所有点的x坐标和y坐标均会分别放大k1和k2倍:

```java
x = k1x0;
y = k2y0;
```

用矩阵表示为：

![](http://offfjcibp.bkt.clouddn.com/scale.png)

### 错切变换

错切变换的效果就是让所有点的x坐标(或者y坐标)保持不变，而对应的y坐标(或者x坐标)则按比例发生平移，且平移的大小和该点到x轴(或y轴)的垂直距离成正比。错切变换，属于等面积变换，即一个形状在错切变换的前后，其面积是相等的。

#### 水平错切

水平错切时y值时不变的，x值增加k1倍的y

```java
x = x0 + k1y0;
y = y0;
```

用矩阵表示是这样的：
![](http://offfjcibp.bkt.clouddn.com/skew1.png)

#### 垂直错切

垂直错切时x值不变，y值增加k2倍的x

```java
x = x0;
y = y0 + k2x0;
```

用矩阵表示是这样的：
![](http://offfjcibp.bkt.clouddn.com/skew2.png)

#### 复合错切

```java
x = x0 + k1y0;
y = k2x0 + y0;
```

用矩阵表示是这样的：
![](http://offfjcibp.bkt.clouddn.com/skew3.png)

```java
Matrix matrix = new Matrix();
//平移 沿x轴移动图片的宽度距离，沿y轴移动图片的高度距离
//matrix.postTranslate(mMatrixImageView.getImageBitmap().getWidth(), mMatrixImageView.getImageBitmap().getHeight());
//mMatrixImageView.setImageMatrix(matrix);

//缩放 以图片中心点缩小一倍
//matrix.postScale(0.5f, 0.5f, mMatrixImageView.getImageBitmap().getWidth() / 2, mMatrixImageView.getImageBitmap().getHeight() / 2);
//mMatrixImageView.setImageMatrix(matrix);

//旋转 绕图片右下角的点旋转180度
//matrix.postRotate(180, mMatrixImageView.getImageBitmap().getWidth(), mMatrixImageView.getImageBitmap().getHeight());
//mMatrixImageView.setImageMatrix(matrix);

// 错切
//matrix.postSkew(0, 1);//垂直错切
//matrix.postSkew(1, 0);//水平错切
//matrix.postSkew(1, 2);//复合错切
//mMatrixImageView.setImageMatrix(matrix);
```

## 使用方法

针对每种变换，Android提供了pre、set和post三种操作方式。其中：

- set用于设置Matrix中的值。
- pre是先乘，因为矩阵的乘法不满足交换律，因此先乘、后乘必须要严格区分。先乘相当于矩阵运算中的右乘。
- post是后乘，因为矩阵的乘法不满足交换律，因此先乘、后乘必须要严格区分。后乘相当于矩阵运算中的左乘。

由于这几种变换方式都是基于矩阵乘法，所以我找了一篇关于讲矩阵乘法的文章，[理解矩阵乘法](http://www.ruanyifeng.com/blog/2015/09/matrix-multiplication.html)。

| 类别      | 方法                                                       | 说明                                       |
| --------- | ---------------------------------------------------------- | ------------------------------------------ |
| 基本方法  | equals、hashCode、toString、toShortString                  | 比较、 获取哈希值、 转换为字符串           |
| 数值操作  | set、reset、setValues、getValues                           | 设置、 重置、 设置数值、 获取数值          |
| 数值计算  | mapPoints、mapRadius、mapRect、mapVectors                  | 计算变换后的数值                           |
| 设置(set) | setConcat、setRotate、setScale、setSkew、setTranslate      | 设置变换                                   |
| 前乘(pre) | preConcat、preRotate、preScale、preSkew、preTranslate      | 前乘变换                                   |
| 后乘(post)| postConcat、postRotate、postScale、postSkew、postTranslate | 后乘变换                                   |
| 特殊方法  | setPolyToPoly、setRectToRect、rectStaysRect、setSinCos     | 一些特殊操作                               |
| 矩阵相关  | invert、isAffine、isIdentity                               | 求逆矩阵、 是否为仿射矩阵、 是否为单位矩阵 |


- reset方法作用是重置当前Matrix(将当前Matrix重置为单位矩阵)。

- set方法没有返回值，有一个参数，作用是将参数Matrix的数值复制到当前Matrix中。如果参数为空，则重置当前Matrix，相当于reset()。

- setValues是把一个数组的值设置给当前Matrix。

- getValues是复制当前Matrix的值给一个数组。

- setPolyToPoly自由变换

```java
boolean setPolyToPoly (
        float[] src,    // 原始数组 src [x,y]，存储内容为一组点
        int srcIndex,   // 原始数组开始位置
        float[] dst,    // 目标数组 dst [x,y]，存储内容为一组点
        int dstIndex,   // 目标数组开始位置
        int pointCount) // 测控点的数量 取值范围是: 0到4


Matrix matrix = new Matrix();		
float[] src = {
		0, 0,                                                                                               // 左上
		mMatrixImageView.getImageBitmap().getWidth(), 0,                                                    // 右上
		mMatrixImageView.getImageBitmap().getWidth(), mMatrixImageView.getImageBitmap().getHeight(),        // 右下
		0, mMatrixImageView.getImageBitmap().getHeight()};                                                  // 左下

float[] dst = {
		88, 99,                                                                                             // 左上
		mMatrixImageView.getImageBitmap().getWidth(), 255,                                                  // 右上
		mMatrixImageView.getImageBitmap().getWidth(), mMatrixImageView.getImageBitmap().getHeight() - 256,  // 右下
		0, mMatrixImageView.getImageBitmap().getHeight()};                                                  // 左下

matrix.setPolyToPoly(src, 0, dst, 0, 4);
mMatrixImageView.setImageMatrix(matrix);
```



其中控制点数量不同，能进行的变换操作也不同，为0的时候不能进行操作，相当于reset，为1的时候只能translate，2的时候可以scale、translate和rotate，3的时候可以scale、translate、rotate和skew，4的时候可以scale、translate、rotate和skew以及任何形变

### pre与post的区别

主要区别其实就是矩阵的乘法顺序不同，pre相当于矩阵的右乘(从左往右进行)，而post相当于矩阵的左乘(从右往左进行)。组合操作构造Matrix时，个人建议尽量全部使用后乘或者全部使用前乘，这样操作顺序容易确定，出现问题也比较容易排查。当然，由于矩阵乘法不满足交换律，前乘和后乘的结果是不同的，使用时应结合具体情景分析使用。


## 参考文章

- [理解矩阵乘法](http://www.ruanyifeng.com/blog/2015/09/matrix-multiplication.html)
- [Android中图像变换Matrix的原理、代码验证和应用](http://biandroid.iteye.com/blog/1399462)
- [安卓自定义View进阶-Matrix原理](http://www.gcssloop.com/customview/Matrix_Basic)
- [安卓自定义View进阶-Matrix详解](http://www.gcssloop.com/customview/Matrix_Method)


