package com.noey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager.widget.ViewPager;
import com.github.mikephil.charting.charts.PieChart;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Locale;
import static org.junit.Assert.*;

/** Exercises the original fragments directly on Android; no production refactor. */
@RunWith(AndroidJUnit4.class)
public class BudgetAcceptanceTest {
    private HomeActivity activity;
    private Locale previousLocale;
    private final android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final int[] outputs = {R.id.text_1,R.id.text_2,R.id.text_3,R.id.text_4,R.id.text_5};
    @Before public void launch() {
        previousLocale = Locale.getDefault(); Locale.setDefault(Locale.US);
        Intent intent = new Intent(instrumentation.getTargetContext(),HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity = (HomeActivity) instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();
    }
    @After public void close() { instrumentation.runOnMainSync(() -> activity.finish()); Locale.setDefault(previousLocale); }
    private void ui(Runnable action) {
        java.util.concurrent.atomic.AtomicReference<Throwable> failure = new java.util.concurrent.atomic.AtomicReference<>();
        instrumentation.runOnMainSync(() -> { try { action.run(); } catch (Throwable e) { failure.set(e); } });
        instrumentation.waitForIdleSync();
        if (failure.get() != null) throw new AssertionError(failure.get());
    }
    private androidx.fragment.app.Fragment page(int index) {
        ui(() -> ((ViewPager)activity.findViewById(R.id.view_pager)).setCurrentItem(index,false));
        return ((UltimateAdapter)((ViewPager)activity.findViewById(R.id.view_pager)).getAdapter()).getItem(index);
    }
    private View root(int index) { return page(index).requireView(); }
    private void set(View root, int id, String value) { ui(() -> ((EditText)root.findViewById(id)).setText(value)); }
    private void click(View root,int id) { ui(() -> root.findViewById(id).performClick()); }
    private void visible(View root,int count,int visibility) {
        ui(() -> {
            for(int i=0;i<count;i++) assertEquals(visibility,root.findViewById(outputs[i]).getVisibility());
            assertEquals(visibility,root.findViewById(R.id.pie_chart).getVisibility());
        });
    }
    private void text(View root,String... expected) {
        ui(() -> { for(int i=0;i<expected.length;i++) assertEquals(expected[i],((TextView)root.findViewById(outputs[i])).getText().toString()); });
    }
    private void chart(View root,float... values) {
        ui(() -> {
            PieChart chart=root.findViewById(R.id.pie_chart);
            com.github.mikephil.charting.interfaces.datasets.IPieDataSet data=chart.getData().getDataSet();
            assertEquals(values.length,data.getEntryCount());
            String[] labels={"Education","Housing","Food","Transport","Balance"};
            int[] colors={Color.rgb(255,99,132),Color.rgb(54,162,235),Color.rgb(255,206,86),Color.rgb(75,192,192),Color.rgb(153,102,255)};
            for(int i=0;i<values.length;i++) {
                assertEquals(values[i],data.getEntryForIndex(i).getValue(),.02f);
                assertEquals(labels[i],data.getEntryForIndex(i).getLabel());
                assertEquals(colors[i],data.getColor(i));
            }
            assertEquals("Expenditures",data.getLabel());
            assertTrue(chart.isHighlightPerTapEnabled());
            assertTrue(chart.getDescription().isEnabled());
            assertEquals("Monthly Expenditures",chart.getDescription().getText());
            // MPAndroidChart converts the supplied text size from dp to pixels.
            assertEquals(12f * activity.getResources().getDisplayMetrics().density,chart.getDescription().getTextSize(),.01f);
            assertEquals(Color.DKGRAY,chart.getDescription().getTextColor());
            assertEquals(900f,chart.getDescription().getPosition().x,0f);
            assertEquals(750f,chart.getDescription().getPosition().y,0f);
        });
    }
    private void capture(String name) throws Exception {
        Thread.sleep(1700);
        File directory = new File(instrumentation.getTargetContext().getExternalFilesDir(null),"verification");
        assertTrue(directory.isDirectory() || directory.mkdirs());
        Bitmap bitmap=instrumentation.getUiAutomation().takeScreenshot(); assertNotNull(bitmap);
        try(FileOutputStream out=new FileOutputStream(new File(directory,name+".png"))) { bitmap.compress(Bitmap.CompressFormat.PNG,100,out); }
        bitmap.recycle();
    }
    private void inject(androidx.fragment.app.Fragment fragment,String field,String value) {
        ui(() -> { try { Field f=fragment.getClass().getDeclaredField(field); f.setAccessible(true); f.set(fragment,value); } catch(Exception e) { throw new AssertionError(e); } });
    }
    @Test public void estimatedNumericalOracleAndChart() {
        View r=root(0);
        String[] inputs={"9856","40000","49999","50000","75000","100000","100001","120000","1"};
        // E-03 uses observed Android Formatter rounding; see verification/RESULTS.md.
        String[][] amounts={{"739.20","3203.20","1724.80","1232.00","2956.80"},{"3000.00","13000.00","7000.00","5000.00","12000.00"},{"3749.92","16249.68","8749.82","6249.88","14999.70"},{"6250.00","13750.00","6250.00","6250.00","17500.00"},{"9375.00","20625.00","9375.00","9375.00","26250.00"},{"12500.00","27500.00","12500.00","12500.00","35000.00"},{"17500.18","22500.23","7500.08","12500.13","40000.40"},{"21000.00","27000.00","9000.00","15000.00","48000.00"},{"0.08","0.33","0.18","0.13","0.30"}};
        String[] prefixes={"Education Expenses: $","Housing Expenses: $","Food Expenses: $","Transport Expenses: $","End-Of-Month Balance: $"};
        java.util.List<String> discrepancies=new java.util.ArrayList<>();
        for(int n=0;n<inputs.length;n++) {
            set(r,R.id.et_income1,inputs[n]);click(r,R.id.btn_plan_budget);visible(r,5,View.VISIBLE);
            for(int i=0;i<5;i++) {
                String actual=((TextView)r.findViewById(outputs[i])).getText().toString();
                String expected=prefixes[i]+amounts[n][i];
                System.out.println("Estimated "+inputs[n]+": "+actual);
                if(!expected.equals(actual)) discrepancies.add(inputs[n]+" expected "+expected+"; actual "+actual);
            }
        }
        set(r,R.id.et_income1,"9856");click(r,R.id.btn_plan_budget);chart(r,739.2f,3203.2f,1724.8f,1232f,2956.8f);
        assertTrue("Documented rounding discrepancies: "+discrepancies,discrepancies.isEmpty());
    }
    private void actualInput(View r,String... values) {
        int[] ids={R.id.et_income,R.id.et_income2,R.id.et_income3,R.id.et_income4};
        for(int i=0;i<4;i++)set(r,ids[i],values[i]);
    }
    @Test public void actualNumericalOracleAndChart() {
        View r=root(1);
        String[][] inputs={{"250","6980","486","666"},{"100","100","100","100"},{"100","200","300","400"},{"1","1","1","3"},{"1","1","1","1"}};
        String[][] amounts={{"2.98","83.27","5.80","7.95"},{"25.00","25.00","25.00","25.00"},{"10.00","20.00","30.00","40.00"},{"16.67","16.67","16.67","50.00"},{"25.00","25.00","25.00","25.00"}};
        for(int n=0;n<inputs.length;n++) {
            actualInput(r,inputs[n]);click(r,R.id.query_btn);visible(r,4,View.VISIBLE);
            text(r,"Transport: "+amounts[n][0]+"%","Education:"+amounts[n][1]+"%","Housing:"+amounts[n][2]+"%","Food:"+amounts[n][3]+"%");
        }
        actualInput(r,"100","200","300","400");click(r,R.id.query_btn);chart(r,200,300,400,100);
    }
    @Test public void validationAndStaleParserOutput() {
        View r=root(0);visible(r,5,View.GONE);click(r,R.id.btn_plan_budget);visible(r,5,View.GONE);
        for(String invalid:new String[]{"","0","0123","0.5"}) {
            set(r,R.id.et_income1,"9856");click(r,R.id.btn_plan_budget);
            inject(page(0),"edStr",invalid);click(r,R.id.btn_plan_budget);visible(r,5,View.GONE);
        }
        set(r,R.id.et_income1,"9856");click(r,R.id.btn_plan_budget);
        inject(page(0),"edStr","abc");click(r,R.id.btn_plan_budget);visible(r,5,View.VISIBLE);chart(r,739.2f,3203.2f,1724.8f,1232,2956.8f);
        View a=root(1);click(a,R.id.query_btn);visible(a,4,View.GONE);
        String[] fields={"edStr","edStr2","edStr3","edStr4"};
        for(String field:fields) for(String invalid:new String[]{"","0","0123","0.5"}) {
            actualInput(a,"100","200","300","400");click(a,R.id.query_btn);
            inject(page(1),field,invalid);click(a,R.id.query_btn);visible(a,4,View.GONE);
        }
        actualInput(a,"100","200","300","400");click(a,R.id.query_btn);
        inject(page(1),"edStr","abc");click(a,R.id.query_btn);visible(a,4,View.VISIBLE);
        text(a,"Transport: 10.00%","Education:20.00%","Housing:30.00%","Food:40.00%");
    }
    @Test public void independentLivePagesAndEditing() {
        View e=root(0);set(e,R.id.et_income1,"9856");click(e,R.id.btn_plan_budget);
        set(e,R.id.et_income1,"50000");chart(e,739.2f,3203.2f,1724.8f,1232,2956.8f);
        View a=root(1);visible(a,4,View.GONE);actualInput(a,"100","200","300","400");click(a,R.id.query_btn);
        root(0);assertEquals("50000",((EditText)e.findViewById(R.id.et_income1)).getText().toString());visible(e,5,View.VISIBLE);
        click(e,R.id.btn_plan_budget);chart(e,6250,13750,6250,6250,17500);
        root(1);chart(a,200,300,400,100);
        assertEquals(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,activity.getRequestedOrientation());
    }
    @Test public void tapAndSwipeNavigation() {
        assertEquals(0,((ViewPager)activity.findViewById(R.id.view_pager)).getCurrentItem());
        View title=((com.flyco.tablayout.SlidingTabLayout)activity.findViewById(R.id.market_tab)).getTitleView(1);
        int[] location=new int[2];title.getLocationOnScreen(location);
        gesture(location[0]+title.getWidth()/2f,location[1]+title.getHeight()/2f,0);
        assertEquals(1,((ViewPager)activity.findViewById(R.id.view_pager)).getCurrentItem());
        View pager=activity.findViewById(R.id.view_pager);pager.getLocationOnScreen(location);
        gesture(location[0]+pager.getWidth()*.15f,location[1]+pager.getHeight()*.4f,pager.getWidth()*.7f);
        assertEquals(0,((ViewPager)activity.findViewById(R.id.view_pager)).getCurrentItem());
        gesture(location[0]+pager.getWidth()*.85f,location[1]+pager.getHeight()*.4f,-pager.getWidth()*.7f);
        assertEquals(1,((ViewPager)activity.findViewById(R.id.view_pager)).getCurrentItem());
    }
    private void gesture(float x,float y,float distance) {
        long down=android.os.SystemClock.uptimeMillis();
        android.view.MotionEvent event=android.view.MotionEvent.obtain(down,down,android.view.MotionEvent.ACTION_DOWN,x,y,0);
        instrumentation.sendPointerSync(event);event.recycle();
        for(int step=1;step<=12;step++) {
            android.os.SystemClock.sleep(25);
            event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),android.view.MotionEvent.ACTION_MOVE,x+distance*step/12f,y,0);
            instrumentation.sendPointerSync(event);event.recycle();
        }
        event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),android.view.MotionEvent.ACTION_UP,x+distance,y,0);
        instrumentation.sendPointerSync(event);event.recycle();android.os.SystemClock.sleep(600);
        instrumentation.waitForIdleSync();
    }
    @Test public void fourReferenceScreenshots() throws Exception {
        View e=root(0);click(e,R.id.btn_plan_budget);capture("estimated-empty");
        set(e,R.id.et_income1,"9856");click(e,R.id.btn_plan_budget);capture("estimated-result");
        View a=root(1);actualInput(a,"250","6980","486","666");click(a,R.id.query_btn);capture("actual-result");
        set(a,R.id.et_income3,"");click(a,R.id.query_btn);capture("actual-incomplete");
    }
}
