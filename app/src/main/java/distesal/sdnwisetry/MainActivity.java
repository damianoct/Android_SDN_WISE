package distesal.sdnwisetry;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.InputStream;
import java.util.List;


public class MainActivity extends Activity
{
    private ListView networkListView;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        networkListView = (ListView) findViewById(R.id.networkListView);
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);

        listAdapter.add("Milardo e Milone");

        networkListView.setAdapter(listAdapter);

        InputStream controllerInput = getResources().openRawResource(
                getResources().getIdentifier("raw/config",
                        "raw", getPackageName()));
        InputStream adaptInput = getResources().openRawResource(
                getResources().getIdentifier("raw/config_adapt",
                        "raw", getPackageName()));
        InputStream flowInput = getResources().openRawResource(
                getResources().getIdentifier("raw/config_flow",
                        "raw", getPackageName()));

        SdnWise sw = new SdnWise(controllerInput,adaptInput,flowInput);
        sw.startExemplaryControlPlane();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
