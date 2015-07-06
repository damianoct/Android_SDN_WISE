package distesal.sdnwisetry;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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
import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Damiano Di Stefano
 * @author Marco Giuseppe Salafia
 */


public class MainActivity extends Activity implements Observer
{
    private ListView networkListView;
    private ArrayAdapter<Node> listAdapter;
    private Comparator<Node> nodeComparator;
    private AsyncTask<Activity, Integer, Void> processoAsync;
    private SdnWise sw;
    private Graph graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processoAsync = new AsyncTask<Activity, Integer, Void>() {
            @Override
            protected Void doInBackground(Activity... activity)
            {
                InputStream controllerInput = getResources().openRawResource(
                        getResources().getIdentifier("raw/config",
                                "raw", getPackageName()));

                sw = new SdnWise(controllerInput,(Observer) activity[0]);
                sw.addObserver((Observer) activity[0]);
                sw.startExemplaryControlPlane();
                graph = sw.getNetworkGraph(sw.getController());
                return null;
            }
        };


        //crisp comparator
        nodeComparator = new Comparator<Node>()
        {
            @Override
            public int compare(Node n1, Node n2)
            {
                if(n1.getId().length() > n2.getId().length())
                    return 1;

                if(n1.getId().length() < n2.getId().length())
                    return -1;

                else
                    return n1.getId().compareTo(n2.getId());
            }
        };

        networkListView = (ListView) findViewById(R.id.networkListView);
        listAdapter = new ArrayAdapter<Node>(this, R.layout.simplerow);

        networkListView.setAdapter(listAdapter);

        networkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(getApplicationContext(), PopUpDetailNode.class);

                ArrayList<String> neighborNodes = new ArrayList<String>();

                Node n = listAdapter.getItem(i);

                for (Edge e : n.getEachEdge())
                {
                    neighborNodes.add(e.getOpposite(n).toString());
                }

                intent.putStringArrayListExtra("lista_nodi_vicini", neighborNodes);
                intent.putExtra("idNodo", listAdapter.getItem(i).getId());
                startActivity(intent);
            }
        });

        processoAsync.execute(this);

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

    @Override
    public void onBackPressed()
    {
        // nothing.
    }

    public boolean nodeExists(Node n)
    {
        if(!listAdapter.isEmpty())
        {
            for(int i = 0; i < listAdapter.getCount(); i++)
            {
                if (listAdapter.getItem(i).getId().equals(n.getId()))
                    return true;
            }
        }

        return false;
    }

    public void removeNodeIfExists(Node n)
    {
        if (!listAdapter.isEmpty()) {
            for (int i = 0; i < listAdapter.getCount(); i++)
            {
                if (listAdapter.getItem(i).getId().equals(n.getId()))
                {
                    listAdapter.remove(listAdapter.getItem(i));
                    listAdapter.sort(nodeComparator);
                }
            }
        }
    }


    public void refreshList(final Node n)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                removeNodeIfExists(n);
                listAdapter.add(n);
                listAdapter.sort(nodeComparator);
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
