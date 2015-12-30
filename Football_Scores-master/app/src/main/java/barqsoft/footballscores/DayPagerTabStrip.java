package barqsoft.footballscores;

import android.content.Context;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

/**
 * Created by Michael Cavalli on 12/18/2015.
 */
public class DayPagerTabStrip extends android.support.v4.view.PagerTabStrip {

    public DayPagerTabStrip (Context c){
        super(c);
    }

    public DayPagerTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * Catch the accessibility event to add content descriptions
     * @param view
     * @param event
     * @return
     */
    @Override
    public boolean onRequestSendAccessibilityEvent(View view, AccessibilityEvent event) {

        String pageTitle = ((TextView) view).getText().toString();
        ViewPager viewPager = (ViewPager) this.getParent();
        int pageIndex = viewPager.getCurrentItem();
        String title = viewPager.getAdapter().getPageTitle(pageIndex).toString();

        if (pageTitle.equals(title)) {
            view.setContentDescription("Current tab " + pageTitle);
        } else {
            view.setContentDescription(pageTitle + " tab");
        }

        return super.onRequestSendAccessibilityEvent(view, event);
    }


}
