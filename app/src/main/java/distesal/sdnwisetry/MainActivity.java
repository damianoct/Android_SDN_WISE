package distesal.sdnwisetry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.sdnwiselab.sdnwise.graphStream.Edge;
import com.github.sdnwiselab.sdnwise.graphStream.Graph;
import com.github.sdnwiselab.sdnwise.graphStream.Node;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class MainActivity extends Activity implements Observer
{
    private ListView networkListView;
    private ArrayAdapter<Node> listAdapter;

    private SdnWise sw;
    private Graph graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        networkListView = (ListView) findViewById(R.id.networkListView);
        listAdapter = new ArrayAdapter<Node>(this, R.layout.simplerow);

        //listAdapter.add("Network Nodes:");


        networkListView.setAdapter(listAdapter);

        networkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(getApplicationContext(), PopUpDetailNode.class);
                //intent.putExtra("nodo", listAdapter.getItem(i));

                ArrayList<String> neighborNodes = new ArrayList<String>();

                Node n = listAdapter.getItem(i);

                for (Edge e : n.getEachEdge())
                {
                    System.out.println("\t\t\t\t" + e.getOpposite(n));
                    neighborNodes.add(e.getOpposite(n).toString());
                }
                intent.putStringArrayListExtra("lista_nodi_vicini", neighborNodes);
                startActivity(intent);
            }
        });

        InputStream controllerInput = getResources().openRawResource(
                getResources().getIdentifier("raw/config",
                        "raw", getPackageName()));
        InputStream adaptInput = getResources().openRawResource(
                getResources().getIdentifier("raw/config_adapt",
                        "raw", getPackageName()));
        InputStream flowInput = getResources().openRawResource(
                getResources().getIdentifier("raw/config_flow",
                        "raw", getPackageName()));

        sw = new SdnWise(controllerInput,adaptInput,flowInput, this);
        sw.addObserver(this);
        sw.startExemplaryControlPlane();
        graph = sw.getNetworkGraph(sw.getController());

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

    public void refreshList(final Node n)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                listAdapter.add(n);
                listAdapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    public void update(Observable observable, Object o)
    {
        if(o != null)
        {
            if(o.equals("Finito"))
            {
                System.out.println("FINITO");
                for(int i = 0; i < listAdapter.getCount(); i++)
                {
                    Node n = listAdapter.getItem(i);
                    System.out.println("\t\tNeighbor Nodo: "+ n.getId());
                    for (Edge e : n.getEachEdge())
                    {
                        System.out.println("\t\t\t\t" + e.getOpposite(n));
                    }
                }
            }
            else
            {
                Node n = (Node) o;
                Node node = graph.getNode(n.getId());
                refreshList(n);
            }
        }

    }
}
