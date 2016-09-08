package com.wtao.myscrollpanel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;

public class ScrollPanelListView extends ListView implements  android.widget.AbsListView.OnScrollListener{
	private View childPanel;
	private int mWidthMeasureSpec;
	private int mHeighMeasureSpec;
	//定义滚动条的y坐标的位置  onScroll里会不断判断和修改
	public int panelPosition=0;
	public Animation mInAnimation = null;
	public Animation mOutAnimation = null;
	//指示器在listView中y轴的高度
	public int thumbOffset = 0;
	private int mLastPosition = -1;
	
	public ScrollPanelListView(Context context){
		super(context);
	}
	public ScrollPanelListView(Context context, AttributeSet attrs) {
		super(context,attrs);
		super.setOnScrollListener(this);
		final TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.ScroolPanelListView);
		final int layoutId = a.getResourceId(R.styleable.ScroolPanelListView_indicator_layout, R.layout.panel);
		final int in = a.getResourceId(R.styleable.ScroolPanelListView_in_anim, R.anim.in);
		final int out = a.getResourceId(R.styleable.ScroolPanelListView_out_anim, R.anim.out);
		a.recycle();
		childPanel =LayoutInflater.from(context).inflate(layoutId, this,false);
		childPanel.setVisibility(View.GONE);
		//提醒自定的View重新调整大小及绘制
		requestLayout();
		mInAnimation = AnimationUtils.loadAnimation(context, in);
		mOutAnimation = AnimationUtils.loadAnimation(context, out);
		mOutAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				if(childPanel!=null){
					childPanel.setVisibility(View.GONE);
				}
			}
		});
	}
	 
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//super.onMeasure() 就可以计算出ListView的尺寸大小，接下来要计算出childPanel的尺寸和摆放的位置计算出来
		if(childPanel!=null && getAdapter()!=null){
			mWidthMeasureSpec = widthMeasureSpec;
			mHeighMeasureSpec = heightMeasureSpec;
			measureChild(childPanel, widthMeasureSpec, heightMeasureSpec);//这个measureChild是ViewGroup的，里面计算了pading然后调用子类的计算的方法
			//measureChild调用完后，childPanel的measuredWidth才有值，但是width是没有值的，width的值要调用完onLayout才有值的
		}
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		//摆放我们的childPanel
		if(childPanel!=null ){
			int left = getMeasuredWidth() - childPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
			childPanel.layout(left, panelPosition,left+childPanel.getMeasuredWidth(), panelPosition+childPanel.getMeasuredHeight());
		}
		
	}
	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.dispatchDraw(canvas);
		if(childPanel!=null && childPanel.getVisibility()==View.VISIBLE){
			//drawChild时，会去调用父类的onMeasure方法和onLayout方法确定当前的childView的大小和位置
			drawChild(canvas,childPanel,getDrawingTime());
		}
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if(childPanel!=null && listener!=null){
			/**
			 computeVerticalScrollExtent()  在纵向滑动访问内高度占用的比例，滑块高度/listView可以滑动的高度（屏幕高度）
			 computeVerticalScrollOffset() 滚动条的纵向幅度的位置  相对于range的值 0--10000
			 computeVerticalScrollRange()  0-10000 纵向滑动条代表的整个纵向范围
			 */
			//1、得到滑块的高度
			int height = Math.round(getMeasuredHeight()*computeVerticalScrollExtent()/computeVerticalScrollRange());
			
			//2、得到滑块的中间位置的y坐标thumbOffset(滑块的顶部+一半高度)
			thumbOffset = height * computeVerticalScrollOffset()/computeVerticalScrollExtent();
			thumbOffset +=height/2;  //滑块的中间坐标和我们的指示块的中间坐标相同
			
			//panel的顶部距离值
			panelPosition = thumbOffset - childPanel.getMeasuredHeight()/2;
			
			int left = getMeasuredWidth() - childPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
			childPanel.layout(left, panelPosition, left+childPanel.getMeasuredWidth(), panelPosition+childPanel.getMeasuredHeight());
			
		
			//然后判断指示器在那个位置
			for(int i=0;i<getChildCount();i++){
				View childView = getChildAt(i);
				if(childView!=null){
					if(thumbOffset>childView.getTop() && thumbOffset<childView.getBottom()){
						if(mLastPosition!=firstVisibleItem){
							mLastPosition = firstVisibleItem+i;
							
							listener.onPositionChanged(this, mLastPosition, childPanel);//回调，重新改变TextView显示的值
							//text长度会变化，所以要重新计算view的宽高
							measureChild(childPanel, mWidthMeasureSpec, mHeighMeasureSpec);
						}
						break;
					}
				}
			}
			
			
			//需要不断修改top值，并且top的位置跟listview的滚动条有关系
			//childPanel.layout(l, t, r, b);
		}
	}
	@Override
	protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
		// TODO Auto-generated method stub
		//判断滚动条是否出现了
		boolean isAnimationPlayed = super.awakenScrollBars(startDelay, invalidate);;
		if(isAnimationPlayed &&childPanel!=null ){
			//滑动的时候，listview会唤醒滑动条，就调用这个方法
			if( childPanel.getVisibility() == View.GONE){
				childPanel.setVisibility(View.VISIBLE);
				if(mInAnimation!=null){
					childPanel.startAnimation(mInAnimation);
				}
			}
			handler.removeCallbacks(outRunnable);
			handler.postAtTime(outRunnable, startDelay+AnimationUtils.currentAnimationTimeMillis());
		}
		return isAnimationPlayed;
	}
	Handler handler = new Handler();
	private final Runnable outRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mOutAnimation!=null){
				childPanel.startAnimation(mOutAnimation);
			}
		}
	};
	public static interface OnPositionChangedListener {
		/**
		 * 
		 * @param listView 当前的ListView
		 * @param position
		 * @param indicatorView 侧边提示的View
		 */
		void onPositionChanged(ScrollPanelListView listView ,int position, View indicatorView);
	}
	OnPositionChangedListener listener;
	public void setOnPositionChangedListener(OnPositionChangedListener listener) {
		this.listener = listener;
	}
}
