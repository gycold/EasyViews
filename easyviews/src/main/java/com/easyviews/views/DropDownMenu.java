package com.easyviews.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyviews.R;
import com.easyviews.views.util.DeviceUtils;

import java.util.List;

/**
 * package: com.easyviews.views.DropDownMenu
 * author: gyc
 * description:分类选择器
 * time: create at 2017/5/15 23:51
 */

public class DropDownMenu extends LinearLayout {
    //顶部菜单布局
    private LinearLayout tabMenuView;
    //底部容器，包含popupMenuViews，maskView
    private FrameLayout containerView;
    //弹出菜单父布局
    private FrameLayout popupMenuViews;
    //遮罩半透明View，点击可关闭DropDownMenu
    private View maskView;
    //tabMenuView里面选中的tab位置，-1表示未选中
    private int current_tab_position = -1;

    //分割线颜色
    private int dividerColor = 0xffcccccc;
    //tab选中颜色
    private int textSelectedColor = 0xff890c85;
    //tab未选中颜色
    private int textUnselectedColor = 0xff111111;
    //遮罩颜色
    private int maskColor = 0x88888888;
    //tab字体大小
    private int menuTextSize = 14;

    //tab高度
    private int menuHeight;

    public static final int MENU_HEIGHT = 45;

    //tab选中图标
    private int menuSelectedIcon;
    //tab未选中图标
    private int menuUnselectedIcon;
    private int menuIconHeight;
    private int menuIconWidth;

    private int menuIconMarginLeft;
    private int menuIconMarginTop;

    public static final int MENU_ICON_HEIGHT = 30;
    public static final int MENU_ICON_WIDTH = 30;

    private float menuHeighPercent = 0.5f;

    public DropDownMenu(Context context) {
        this(context, null);
    }

    public DropDownMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropDownMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        //为DropDownMenu添加自定义属性
        int menuBackgroundColor = 0xffffffff;
        int underlineColor = 0xffcccccc;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DropDownMenu);
        underlineColor = a.getColor(R.styleable.DropDownMenu_ddunderlineColor, underlineColor);
        dividerColor = a.getColor(R.styleable.DropDownMenu_dddividerColor, dividerColor);
        textSelectedColor = a.getColor(R.styleable.DropDownMenu_ddtextSelectedColor, textSelectedColor);
        textUnselectedColor = a.getColor(R.styleable.DropDownMenu_ddtextUnselectedColor, textUnselectedColor);
        menuBackgroundColor = a.getColor(R.styleable.DropDownMenu_ddmenuBackgroundColor, menuBackgroundColor);
        maskColor = a.getColor(R.styleable.DropDownMenu_ddmaskColor, maskColor);
        menuTextSize = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuTextSize, menuTextSize);
        menuSelectedIcon = a.getResourceId(R.styleable.DropDownMenu_ddmenuSelectedIcon, menuSelectedIcon);
        menuUnselectedIcon = a.getResourceId(R.styleable.DropDownMenu_ddmenuUnselectedIcon, menuUnselectedIcon);
        menuHeighPercent = a.getFloat(R.styleable.DropDownMenu_ddmenuMenuHeightPercent, menuHeighPercent);
        menuHeight = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuHeight, MENU_HEIGHT);
        menuIconHeight = a.getDimensionPixelSize(R.styleable.DropDownMenu_ddmenuIconHeight, MENU_ICON_HEIGHT);
        menuIconWidth = a.getDimensionPixelOffset(R.styleable.DropDownMenu_ddmuneIconWidth, MENU_ICON_WIDTH);
        menuIconMarginLeft = a.getDimensionPixelOffset(R.styleable.DropDownMenu_ddmenuIconMarginLeft, 0);
        menuIconMarginTop = a.getDimensionPixelOffset(R.styleable.DropDownMenu_ddmenuIconMarginTop, 0);

        a.recycle();

        //初始化tabMenuView并添加到tabMenuView
        tabMenuView = new LinearLayout(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = menuHeight;
        tabMenuView.setOrientation(HORIZONTAL);
        tabMenuView.setBackgroundColor(menuBackgroundColor);
        tabMenuView.setLayoutParams(params);
        addView(tabMenuView, 0);

        //为tabMenuView添加下划线
        View underLine = new View(getContext());
        underLine.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2Px(0.5f)));
        underLine.setBackgroundColor(underlineColor);
        addView(underLine, 1);

        //初始化containerView并将其添加到DropDownMenu
        containerView = new FrameLayout(context);
        containerView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        addView(containerView, 2);

    }

    public void setMenuHeight(int height) {
        LayoutParams params = (LayoutParams) tabMenuView.getLayoutParams();
        params.height = menuHeight;
        tabMenuView.setLayoutParams(params);
    }

    public interface OnMenuExtraClicked {
        void onMenuExtraClicked();
    }
    private OnMenuExtraClicked onMenuExtraClicked;

    public void setDropDownMenuWithExtra(@NonNull List<String> tabTexts, @NonNull List<View> popupViews, @NonNull View contentView, OnMenuExtraClicked onMenuExtraClicked) {
        if (tabTexts.size() != popupViews.size()) {
            throw new IllegalArgumentException("params not match, tabTexts.size() should be equals popupViews.size()");
        }

        for (int i = 0; i < tabTexts.size(); i++) {
            addTab(tabTexts, i);
        }

        this.onMenuExtraClicked = onMenuExtraClicked;
        addTabExtra();

        containerView.addView(contentView, 0);

        maskView = new View(getContext());
        maskView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        maskView.setBackgroundColor(maskColor);
        maskView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
        containerView.addView(maskView, 1);
        maskView.setVisibility(GONE);
        if (containerView.getChildAt(2) != null) {
            containerView.removeViewAt(2);
        }

        popupMenuViews = new FrameLayout(getContext());
        popupMenuViews.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (DeviceUtils.getScreenSize(getContext()).y * menuHeighPercent)));
        popupMenuViews.setVisibility(GONE);
        containerView.addView(popupMenuViews, 2);

        for (int i = 0; i < popupViews.size(); i++) {
            popupViews.get(i).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            popupMenuViews.addView(popupViews.get(i), i);
        }
    }

    /**
     * 初始化DropDownMenu
     *
     * @param tabTexts
     * @param popupViews
     * @param contentView
     */
    public void setDropDownMenu(@NonNull List<String> tabTexts, @NonNull List<View> popupViews, @NonNull View contentView) {
        if (tabTexts.size() != popupViews.size()) {
            throw new IllegalArgumentException("params not match, tabTexts.size() should be equals popupViews.size()");
        }

        for (int i = 0; i < tabTexts.size(); i++) {
            addTab(tabTexts, i);
        }
        containerView.addView(contentView, 0);

        maskView = new View(getContext());
        maskView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        maskView.setBackgroundColor(maskColor);
        maskView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
        containerView.addView(maskView, 1);
        maskView.setVisibility(GONE);
        if (containerView.getChildAt(2) != null) {
            containerView.removeViewAt(2);
        }

        popupMenuViews = new FrameLayout(getContext());
        popupMenuViews.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (DeviceUtils.getScreenSize(getContext()).y * menuHeighPercent)));
        popupMenuViews.setVisibility(GONE);
        containerView.addView(popupMenuViews, 2);

        for (int i = 0; i < popupViews.size(); i++) {
            popupViews.get(i).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            popupMenuViews.addView(popupViews.get(i), i);
        }

    }

    private void addTab(@NonNull List<String> tabTexts, int i) {
        LinearLayout tabLayout = new LinearLayout(getContext());
        tabLayout.setGravity(Gravity.CENTER);
        tabLayout.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabLayout.setPadding(dp2Px(5), dp2Px(12), dp2Px(5), dp2Px(12));
        tabLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMenu(tabLayout);
            }
        });
        TextView tab = new TextView(getContext());
        tab.setSingleLine();
        tab.setEllipsize(TextUtils.TruncateAt.END);
        tab.setGravity(Gravity.CENTER);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, menuTextSize);
        tab.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tab.setTextColor(textUnselectedColor);
//        tab.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(menuUnselectedIcon), null);
        tab.setText(tabTexts.get(i));
//        tab.setPadding(dp2Px(5), dp2Px(12), dp2Px(5), dp2Px(12));
        //添加点击事件
        ImageView img = new ImageView(getContext());
        LayoutParams params = new LayoutParams(menuIconWidth, menuIconHeight);
        img.setLayoutParams(params);
        MarginLayoutParams marginParams = null;
        if (img.getLayoutParams() instanceof MarginLayoutParams) {
            marginParams = (MarginLayoutParams) img.getLayoutParams();
        } else {
            marginParams = new MarginLayoutParams(img.getLayoutParams());
        }
        marginParams.setMargins(menuIconMarginLeft, menuIconMarginTop, 0, 0);
        img.setLayoutParams(marginParams);
        img.setImageDrawable(ContextCompat.getDrawable(getContext(), menuUnselectedIcon));
        tabLayout.addView(tab);
        tabLayout.addView(img);
        tabMenuView.addView(tabLayout);
        //添加分割线
        if (i < tabTexts.size() - 1) {
            View view = new View(getContext());
            view.setLayoutParams(new LayoutParams(dp2Px(0.5f), ViewGroup.LayoutParams.MATCH_PARENT));
            view.setBackgroundColor(dividerColor);
            tabMenuView.addView(view);
        }
    }

    private void addTabExtra() {
        View view = new View(getContext());
        view.setLayoutParams(new LayoutParams(dp2Px(0.5f), ViewGroup.LayoutParams.MATCH_PARENT));
        view.setBackgroundColor(dividerColor);
        tabMenuView.addView(view);

        LinearLayout tabLayout = new LinearLayout(getContext());
        tabLayout.setTag(Boolean.TRUE);
        tabLayout.setGravity(Gravity.CENTER);
        tabLayout.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabLayout.setPadding(dp2Px(5), dp2Px(12), dp2Px(5), dp2Px(12));
        tabLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_tab_position != -1) {
                    closeMenu();
                }
                if (onMenuExtraClicked != null) {
                    onMenuExtraClicked.onMenuExtraClicked();
                }
            }
        });
        TextView tab = new TextView(getContext());
        tab.setSingleLine();
        tab.setEllipsize(TextUtils.TruncateAt.END);
        tab.setGravity(Gravity.CENTER);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, menuTextSize);
        tab.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tab.setTextColor(textUnselectedColor);
//        tab.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(menuUnselectedIcon), null);
        tab.setText("筛选");
//        tab.setPadding(dp2Px(5), dp2Px(12), dp2Px(5), dp2Px(12));
        //添加点击事件
        ImageView img = new ImageView(getContext());
        LayoutParams params = new LayoutParams(menuIconWidth, menuIconHeight);
        img.setLayoutParams(params);
        MarginLayoutParams marginParams = null;
        if (img.getLayoutParams() instanceof MarginLayoutParams) {
            marginParams = (MarginLayoutParams) img.getLayoutParams();
        } else {
            marginParams = new MarginLayoutParams(img.getLayoutParams());
        }
        marginParams.setMargins(menuIconMarginLeft, menuIconMarginTop, 0, 0);
        img.setLayoutParams(marginParams);
        img.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_filter));
        tabLayout.addView(tab);
        tabLayout.addView(img);
        tabMenuView.addView(tabLayout);
    }

    /**
     * 改变tab文字
     *
     * @param text
     */
    public void setTabText(String text) {
        if (current_tab_position != -1) {
            ((TextView) ((LinearLayout) tabMenuView.getChildAt(current_tab_position)).getChildAt(0)).setText(text);
        }
    }

    /**
     * 改变tab其他项的文字
     * @param text
     * @param tabPositon
     */
    public void setTabText(String text, int tabPositon) {
        if (current_tab_position != -1) {
            ((TextView) ((LinearLayout) tabMenuView.getChildAt(tabPositon)).getChildAt(0)).setText(text);
        }
    }

    public void setTabClickable(boolean clickable) {
        for (int i = 0; i < tabMenuView.getChildCount(); i = i + 2) {
            tabMenuView.getChildAt(i).setClickable(clickable);
        }
    }

    /**
     * 关闭菜单
     */
    public void closeMenu() {
        if (current_tab_position != -1) {
            ((TextView) ((LinearLayout) tabMenuView.getChildAt(current_tab_position)).getChildAt(0)).setTextColor(textUnselectedColor);
            ((ImageView) ((LinearLayout) tabMenuView.getChildAt(current_tab_position)).getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(getContext(), menuUnselectedIcon));
            popupMenuViews.setVisibility(View.GONE);
            popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
            maskView.setVisibility(GONE);
            maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
            current_tab_position = -1;
        }
    }

    /**
     * DropDownMenu是否处于可见状态
     *
     * @return
     */
    public boolean isShowing() {
        return current_tab_position != -1;
    }

    /**
     * 切换菜单
     *
     * @param target
     */
    private void switchMenu(View target) {
        System.out.println(current_tab_position);
        for (int i = 0; i < tabMenuView.getChildCount(); i = i + 2) {
            if (target == tabMenuView.getChildAt(i)) {
                if (current_tab_position == i) {
                    closeMenu();
                } else {
                    if (current_tab_position == -1) {
                        popupMenuViews.setVisibility(View.VISIBLE);
                        popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
                        maskView.setVisibility(VISIBLE);
                        maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
                        popupMenuViews.getChildAt(i / 2).setVisibility(View.VISIBLE);
                    } else {
                        popupMenuViews.getChildAt(i / 2).setVisibility(View.VISIBLE);
                    }
                    current_tab_position = i;
                    ((TextView) ((LinearLayout) tabMenuView.getChildAt(i)).getChildAt(0)).setTextColor(textSelectedColor);
                    ((ImageView) ((LinearLayout) tabMenuView.getChildAt(i)).getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(getContext(), menuSelectedIcon));
                }
            } else {
                ((TextView) ((LinearLayout) tabMenuView.getChildAt(i)).getChildAt(0)).setTextColor(textUnselectedColor);

                Object tag = tabMenuView.getChildAt(i).getTag();
                if (tag != null && tag instanceof Boolean && ((Boolean) tag)) {
                    ((ImageView) ((LinearLayout) tabMenuView.getChildAt(i)).getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_filter));
                } else {
                    ((ImageView) ((LinearLayout) tabMenuView.getChildAt(i)).getChildAt(1)).setImageDrawable(ContextCompat.getDrawable(getContext(), menuUnselectedIcon));
                }

                if (popupMenuViews.getChildAt(i / 2) != null) {
                    popupMenuViews.getChildAt(i / 2).setVisibility(View.GONE);
                }
            }
        }
    }

    public int dp2Px(float value) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, dm) + 0.5);
    }
}
