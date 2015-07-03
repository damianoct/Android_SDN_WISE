package distesal.sdnwisetry;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

public class PopUpDetailNode extends Activity {

    private ListView networkListView;
    private ArrayAdapter<String> listAdapter;
    private TextView nodeIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_detail_node);

        networkListView = (ListView) findViewById(R.id.neighborListView);
        nodeIdTextView = (TextView) findViewById(R.id.nodeIdtextView);
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
        networkListView.setAdapter(listAdapter);

        nodeIdTextView.append(" " + getIntent().getStringExtra("idNodo"));
        ArrayList<String> neighbors = getIntent().getStringArrayListExtra("lista_nodi_vicini");
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(neighbors);

        for(String neighbor: hs)
        {
            listAdapter.add(neighbor);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pop_up_detail_node, menu);
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
