# SelectLayout
SelectLayout一个选中放大的容器。
# 先看看效果吧
![这里写图片描述](http://img.blog.csdn.net/20170821143320191?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTEzNTY2Mg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 
#整体思路
从设计那里得知要这么一个效果时候，第一反应就是用ViewPager或者画廊等去实现，也找了对应的资料，但是最终效果都不是很满意，给两个ViewPager实现类似效果的链接[仿土巴兔](http://blog.csdn.net/hanhailong726188/article/details/48780329) 和[ViewPagerCards](https://github.com/rubensousa/ViewPagerCards)。
 最终还是通过最基本的平移动画和缩放动画效果实现的，自定义一个继承自RelativeLayout的Layout，至于为什么是RelativeLayout后面说到哈。通过放大中间的一个View，然后每次点击或者滑动出发切换时候，通过动画平移和缩放子View。接下来一起来看看代码。
 
##自定义属性

```
<declare-styleable name="SelectLayout">
        <!--大小球中间间隔和小球的缩放比例-->
        <attr name="spaceScale" format="float"/>
        <!--大小球的缩放比例-->
        <attr name="scale" format="float"/>
        <!--是否相应滑动事件-->
        <attr name="isMoveScrollable" format="boolean"/>
        <!--动画时长-->
        <attr name="animTime" format="integer"/>
 </declare-styleable>
```
 这里提供了四各自定义属性，注释都写得很清楚了。
 其中spaceScale这个值，大小View中间的间隔占小View的比例，如小View宽80dp，间距想设置为20dp,那么这个值应该为0.25；其中spaceScale这个值，是view放大的比例，如80dp->100dp，那么这个值就是（100-80）/80=0.25；

下面是自定义值的获取，记得后面要调用recycle()

```
 //自定义属性的获取
        TypedArray ta=context.obtainStyledAttributes(attrs, R.styleable.SelectLayout,defStyleAttr,0);
        isMoveScrollable=ta.getBoolean(R.styleable.SelectLayout_isMoveScrollable,true);
        mScale=ta.getFloat(R.styleable.SelectLayout_scale,0.3f);
        mSpaceScale=ta.getFloat(R.styleable.SelectLayout_spaceScale,0.2f);
        mAnimTime=ta.getInt(R.styleable.SelectLayout_spaceScale,500);
        ta.recycle();
```

## 子View相关属性值获取
 接下来就需要获取子View以及他们的宽高，在onFinishInflate()方法里面可以获取到View，这个方法表示着ViewGroup已经完成了布局：
 

```
@Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }
    /**
     * 初始化View
     */
    public void initView() {
        viewLeft=findViewById(R.id.viewLeft);
        viewCenter=findViewById(R.id.viewCenter);
        viewRight=findViewById(R.id.viewRight);

        viewLeft.setOnClickListener(this);
        viewCenter.setOnClickListener(this);
        viewRight.setOnClickListener(this);
    }
```
然后再onSizeChanged()方法里面可以获取到子View的一些宽高属性：

```
@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSize();
    }

    /**
     * 初始化View大小 缩放比例
     */
    private void initSize() {
        //两边小的宽度
        mMinWidth=viewLeft.getMeasuredWidth();
        //按照比例设置间距
        mSpaceWidth=mMinWidth*mSpaceScale;
        //中间放大的宽度
        mMaxWidth=mMinWidth*(1+mScale);
        //默认中间一个View放大
        viewCenter.setScaleX(1+mScale);
        viewCenter.setScaleY(1+mScale);

        if(mMaxWidth>getMeasuredHeight()){//缩放法高度大于Layout高度  重新设置高度 自适应
            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)mMaxWidth));
        }

        //设置中间布局margin
        LayoutParams params= (LayoutParams) viewCenter.getLayoutParams();
        params.leftMargin= (int) (mSpaceWidth+mMinWidth*mScale/2);//间距加上放大的边距
        params.rightMargin= (int) (mSpaceWidth+mMinWidth*mScale/2);
        viewCenter.setLayoutParams(params);
    }
```
这里要注意，调用的是getMeasuredWidth(),而不是getWidth(),getWidth()此时还是0，只有当已经显示在屏幕上面才会有值的。

##动画实现基本效果
 其实动画很简单，这里面主要用到了属性动画，完成平移以及缩放。先来分析从最初的状态，点击左边时候的整个过程：
 ![这里写图片描述](http://img.blog.csdn.net/20170821153526433?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMTEzNTY2Mg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 左是从左往右平移一个 左边View一半+间隔+放大后的View宽度的一半，然后放大mScale；
 中是从左往右平移一个 左边View一半+间隔+放大后的View宽度的一半，然后缩小mScale；
 又是从左往左平移一个 （左边View一半+间隔+放大后的View宽度的一半)*2；

```
if(currentSelect== CurrentSelect.center) {//左边到中间
                //左边View平移 方大
                ObjectAnimator animator1 = ObjectAnimator.ofFloat(viewLeft, "translationX", 0, mMinWidth/2+mMaxWidth/2+mSpaceWidth);
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(viewLeft, "scaleX", 1, 1+mScale);
                ObjectAnimator animator3 = ObjectAnimator.ofFloat(viewLeft, "scaleY", 1, 1+mScale);
                //中间View 右移 缩小
                ObjectAnimator animator4 = ObjectAnimator.ofFloat(viewCenter, "translationX",0, mMinWidth/2+mMaxWidth/2+mSpaceWidth);
                ObjectAnimator animator5 = ObjectAnimator.ofFloat(viewCenter, "scaleX", 1+mScale,1f);
                ObjectAnimator animator6 = ObjectAnimator.ofFloat(viewCenter, "scaleY", 1+mScale,1f);
                //右边View 左移
                ObjectAnimator animator7 = ObjectAnimator.ofFloat(viewRight, "translationX",0, -(mMinWidth+mMaxWidth+mSpaceWidth*2));

                playAnim(CurrentSelect.left,animator1, animator2, animator3,animator4, animator5, animator6,animator7);
            }
```
其他情况都是类似的逻辑，就不赘述了。需要注意的是，动画的起始位置和结束位置都是相对于最开始的布局的，动画只是改变view的表现形式，并不会改变view真正的位置。
 
##滑动切换
点击按钮切换基本已经实现了，下面就来看看怎么实现滑动实现。自然是获取到滑动到距离，是否超过一定距离，判断是左滑还是右滑，然后切换选中状态。由于涉及到子View的点击事件，所以需要用到事件分发：

```
float downX=0;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(isMoveScrollable) {//响应滑动事件  事件分发
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    if (Math.abs(moveX - downX) > 50 && moveX > downX) {//右滑
                        downX = moveX;
                        if (currentSelect == CurrentSelect.left) {
                            onItemClick(false,3);
                        } else if (currentSelect == CurrentSelect.center) {
                            onItemClick(false,1);
                        } else {
                            onItemClick(false,2);
                        }
                        return true;
                    } else if (Math.abs(moveX - downX) > 50 && moveX < downX) {//左滑
                        downX = moveX;
                        if (currentSelect == CurrentSelect.left) {
                            onItemClick(false,2);
                        } else if (currentSelect == CurrentSelect.center) {
                            onItemClick(false,3);
                        } else {
                            onItemClick(false,1);
                        }
                        return true;
                    }
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }
```
 我们这里只需要关心X轴值的变化就行，在move里面判断是左滑或者右滑后，切换选中view，并且返回true表示本身消费了这次事件，不会再往下分发该事件。
 
## View的完善
外部在使用Layout的时候，肯定需要知道我们当前选中的View，以及状态切换的监听。

```
/**
     * 选中状态更改回调
     */
    public interface OnSelectChangedListener{
        /**
         * 选中项改变
         * @param current
         */
        public void onSelectChange(CurrentSelect current);

        /**
         * 选中项被点击
         */
        public void onSelectClick();
    }
```
还有一些其他属性的设置

```
/**
     * 设置动画时间
     * @param animTime
     */
    public void setAnimTime(int animTime) {
        mAnimTime = animTime;
    }

    /**
     * 是否能够滑动
     * @param moveScrollable
     */
    public void setMoveScrollable(boolean moveScrollable) {
        isMoveScrollable = moveScrollable;
    }
```


## 说明
这个Layout里面获取子View是根据子View的id获取的，所以务必将三个字View的Id分别设置为viewLeft、viewCenter、viewRight。好吧，我承认这是一个很操蛋的地方，如果不这样，那么就通过getChildAt()索引获取，那样也有一定的限制。有需要就自行修改吧。我只是做个笔记

最后附上源码地址：[https://github.com/HanJinLiang/SelectLayout](https://github.com/HanJinLiang/SelectLayout)

