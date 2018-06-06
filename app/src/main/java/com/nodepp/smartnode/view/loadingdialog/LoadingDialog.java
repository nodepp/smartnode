package com.nodepp.smartnode.view.loadingdialog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nodepp.smartnode.R;
/**
 * Created by yuyue on 2016/9/9.
 */
public class LoadingDialog extends AlertDialog {

	private static final int DELAY = 150;
	private static final int DURATION = 1500;

	private int size;
	private AnimatedView[] spots;
	private AnimatorPlayer animator;

	private TextView tipTextView;
	private String title = "";
	public LoadingDialog(Context context, String title) {
		this(context, R.style.SpotsDialogDefault);//通过资源文件获取style
		this.title = title;
	}

	public LoadingDialog(Context context, int theme) {
		super(context, theme);
	}

	public LoadingDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_loading);
		setCanceledOnTouchOutside(false);

		initProgress(title);
	}

	@Override
	protected void onStart() {
		super.onStart();

		animator = new AnimatorPlayer(createAnimations());
		animator.play();
	}

	@Override
	protected void onStop() {
		super.onStop();

		animator.stop();
	}

    //设置标题名称
	public void setText(String title) {
		tipTextView.setText(title);
	}

	private void initProgress(String title) {
		ProgressLayout progress = (ProgressLayout) findViewById(R.id.progress);
		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
		if (Integer.parseInt(Build.VERSION.SDK) < 19){
			pb.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
		}
		size = progress.getSpotsCount();
		tipTextView = (TextView) findViewById(R.id.title);
		tipTextView.setText(title);
		spots = new AnimatedView[size];
		int size = getContext().getResources().getDimensionPixelSize(
				R.dimen.spot_size);
		int progressWidth = getContext().getResources().getDimensionPixelSize(
				R.dimen.progress_width);
		for (int i = 0; i < spots.length; i++) {//生成点
			AnimatedView v = new AnimatedView(getContext());
			if (i == 0) {
				v.setBackgroundResource(R.drawable.dialog_spot1);
			}else if (i == 1){
				v.setBackgroundResource(R.drawable.dialog_spot2);
			}else if (i == 2){
				v.setBackgroundResource(R.drawable.dialog_spot3);
			}else if (i == 3){
				v.setBackgroundResource(R.drawable.dialog_spot4);
			}else {
				v.setBackgroundResource(R.drawable.dialog_spot);
			}
			v.setTarget(progressWidth);
			v.setXFactor(-1f);
			progress.addView(v, size, size);
			spots[i] = v;
		}
	}

	private Animator[] createAnimations() {//创建点的移动动画
		Animator[] animators = new Animator[size];
		for (int i = 0; i < spots.length; i++) {
			Animator move = ObjectAnimator.ofFloat(spots[i], "xFactor", 0, 1);
			move.setDuration(DURATION);
			move.setInterpolator(new HesitateInterpolator());
			move.setStartDelay(DELAY * i);
			animators[i] = move;
		}
		return animators;
	}
}
