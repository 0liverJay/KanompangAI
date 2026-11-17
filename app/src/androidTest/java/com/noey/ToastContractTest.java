package com.noey;

import android.content.Intent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager.widget.ViewPager;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/** Observes real Toast accessibility events, including validation precedence. */
@RunWith(AndroidJUnit4.class)
public class ToastContractTest {
    @Test public void exactMessagesAndValidationPrecedence() throws Exception {
        android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();
        Intent intent=new Intent(i.getTargetContext(),HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        HomeActivity activity=(HomeActivity)i.startActivitySync(intent);
        try {
            i.waitForIdleSync();
            UltimateAdapter adapter=(UltimateAdapter)((ViewPager)activity.findViewById(R.id.view_pager)).getAdapter();
            androidx.fragment.app.Fragment e=adapter.getItem(0);
            expect(i,e.requireView(),R.id.btn_plan_budget,"Numbers cannot be null.");
            inject(i,e,"edStr","0");expect(i,e.requireView(),R.id.btn_plan_budget,"Numbers cannot start with 0.");
            inject(i,e,"edStr","abc");expect(i,e.requireView(),R.id.btn_plan_budget,"Invalid number format.");
            i.runOnMainSync(() -> ((ViewPager)activity.findViewById(R.id.view_pager)).setCurrentItem(1,false));
            i.waitForIdleSync();androidx.fragment.app.Fragment a=adapter.getItem(1);
            expect(i,a.requireView(),R.id.query_btn,"content is empty");
            inject(i,a,"edStr","0");inject(i,a,"edStr2","");inject(i,a,"edStr3","100");inject(i,a,"edStr4","100");
            expect(i,a.requireView(),R.id.query_btn,"content is empty");
            inject(i,a,"edStr2","abc");expect(i,a.requireView(),R.id.query_btn,"Numbers cannot start with 0.");
            inject(i,a,"edStr","100");expect(i,a.requireView(),R.id.query_btn,"Invalid number format.");
        } finally { i.runOnMainSync(activity::finish); }
    }
    private void inject(android.app.Instrumentation i,Object fragment,String name,String value) throws Exception {
        Field f=fragment.getClass().getDeclaredField(name);f.setAccessible(true);
        i.runOnMainSync(() -> { try { f.set(fragment,value); } catch(Exception e) { throw new AssertionError(e); } });
    }
    private void expect(android.app.Instrumentation i,View root,int button,String text) throws Exception {
        Thread.sleep(2300); // Avoid queued Toasts obscuring the next assertion.
        AccessibilityEvent event=i.getUiAutomation().executeAndWaitForEvent(
            () -> i.runOnMainSync(() -> root.findViewById(button).performClick()),
            e -> e.getEventType()==AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && e.getText().contains(text),5000);
        assertTrue(event.getText().contains(text));event.recycle();
    }
}
