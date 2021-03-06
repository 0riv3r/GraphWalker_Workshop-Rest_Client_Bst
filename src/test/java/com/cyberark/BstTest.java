package com.cyberark;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * REST
 *
 */
public class BstTest {

    private Bst<Integer> bst;
    private ArrayList<Integer> vals; // values to be inserted in the tree
    private ArrayList<Integer> fakeVals; // values that are not inserted in the tree
    private HashSet<Integer> inTree; // the current values in the tree, use set to avoid duplicates (like the tree
                                     // does)
    private Stack<Integer> nodesStack; // used to pop leaves in deletion test, in order to delete leaves
    private Random rand;
    private boolean boolResult;

    public void e_Add() {
        System.out.println("e_Add");
        int val = vals.get(rand.nextInt(vals.size()));
        bst.add(val);
        if (inTree.add(val)) // Add to the stack only if succeeded to add to the set
            nodesStack.push(val);
    }

    public void e_Find() {
        System.out.println("e_Find");

        System.out.println("bst.nodes: " + Arrays.toString(bst.nodes().toArray()));
        System.out.println("inTree: " + Arrays.toString(inTree.toArray()));
        System.out.println("nodesStack: " + Arrays.toString(nodesStack.toArray()));

        // convert HashSet to an array to fetch element by random index
        Integer[] arrInTreeVals = inTree.toArray(new Integer[inTree.size()]);
        int randomIndex = rand.nextInt(inTree.size());
        boolResult = bst.find(arrInTreeVals[randomIndex]);
    }

    public void e_FindFakeVal() {
        System.out.println("e_FindFakeVal");
        boolResult = bst.find(fakeVals.get(rand.nextInt(fakeVals.size())));
    }

    public void e_Init() {
        System.out.println("e_Init");
        bst = new Bst<Integer>();
        vals = new ArrayList<Integer>(Arrays.asList(1, 3, 4, 6, 7, 8, 10, 13, 14));
        fakeVals = new ArrayList<Integer>(Arrays.asList(21, 23, 24, 26, 27, 28, 30, 33, 34));
        inTree = new HashSet<Integer>();
        nodesStack = new Stack<Integer>();
        rand = new Random();
        boolResult = false;
    }

    public void v_Init() {
        System.out.println("v_Init");
        assertNotNull(bst);
        assertNotNull(vals);
        assertNotNull(fakeVals);
        assertNotNull(inTree);
        assertNotNull(rand);
        assertEquals(false, boolResult);

        // System.out.println( "bst.nodes: " + Arrays.toString(bst.nodes().toArray()));
        // System.out.println( "inTree: " + Arrays.toString(inTree.toArray()));
        // System.out.println( "nodesStack: " + Arrays.toString(nodesStack.toArray()));
    }

    public void e_Delete() {
        System.out.println("e_Delete");

        System.out.println("bst.nodes: " + Arrays.toString(bst.nodes().toArray()));
        System.out.println("inTree: " + Arrays.toString(inTree.toArray()));
        System.out.println("nodesStack: " + Arrays.toString(nodesStack.toArray()));

        // The last inserted value is a leaf and should be deleted
        int valToDelete = nodesStack.pop();
        inTree.remove(valToDelete);
        bst.delete(valToDelete);
    }

    public void e_GetNodes() {
        System.out.println("e_GetNodes");
        Set<Integer> expectedNodes = new HashSet<Integer>();
        expectedNodes.addAll(inTree);
        if (expectedNodes.size() == bst.nodes().size()) {
            expectedNodes.removeAll(bst.nodes());
            boolResult = (expectedNodes.size() == 0);
        } else {
            boolResult = false;
        }
    }

    public void e_ToMenu() {
        System.out.println("e_ToMenu");
        // throw new RuntimeException( "e_ToMenu is not implemented yet!" );
    }

    public void v_Added() {
        System.out.println("v_Added");

        System.out.println("bst.nodes: " + Arrays.toString(bst.nodes().toArray()));
        System.out.println("inTree: " + Arrays.toString(inTree.toArray()));
        System.out.println("nodesStack: " + Arrays.toString(nodesStack.toArray()));
        assertEquals(inTree.size(), bst.nodes().size());

        /**
         *  Set the model's data - number of vals to the current number of nodes in the test 
         *  This is because the model doesn't know if a value was added or not
         *  A value will not be added if it is a duplicate
         *  The number of vals in the model must be synced with the number of nodes in the test
         *  Otherwise, the model's guards will not function as they should
         *  The model's guard for instance will enable e_Delete when the tree is empty
         * */ 
        setModelNumberOfNodes(inTree.size());
    }

    public void v_Found() {
        System.out.println("v_Found");
        assertTrue(boolResult, "Find failed!");
    }

    public void v_NotFound() {
        System.out.println("v_NotFound");
        assertFalse(boolResult, "Found a faked value!");
    }

    public void v_Deleted() {
        System.out.println("v_Deleted");

        System.out.println("bst.nodes: " + Arrays.toString(bst.nodes().toArray()));
        System.out.println("inTree: " + Arrays.toString(inTree.toArray()));
        System.out.println("nodesStack: " + Arrays.toString(nodesStack.toArray()));

        assertEquals(inTree.size(), bst.nodes().size());
    }

    public void v_NodesList() {
        System.out.println("v_NodesList");
        assertTrue(boolResult);
    }

    public void v_Start() {
        System.out.println("v_Start");
    }

    public void v_MenuDispatcher() {
        System.out.println("v_MenuDispatcher");
        // throw new RuntimeException( "v_MenuDispatcher is not implemented yet!" );
    }

    /* REST helper methods */

    final static private String baseUrl = "http://localhost:8887/graphwalker/";

    private int getModelNumberOfNodes() {

        String strResponse = "";
        try {
            HttpEntity httpEntity = getRequest("getData");
            strResponse = EntityUtils.toString(httpEntity);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /**
         * response format: { "data": { "nodes": "1" }, "result": "ok" }
         */
        JSONObject jsonResponse = new JSONObject(strResponse);

        String strNodes = jsonResponse.getJSONObject("data").getString("nodes");
        // System.out.println("nodes = " + strNodes);

        return (Integer.parseInt(strNodes));
    }

    private void setModelNumberOfNodes(int vals) {

        String strTarget = "setData/vals=" + String.valueOf(vals);
        try {
            putRequest(strTarget);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private HttpEntity getRequest(String targetUrl) throws Exception {

        DefaultHttpClient httpGetClient = new DefaultHttpClient();
        final HttpGet request = new HttpGet(baseUrl + targetUrl);
        HttpResponse response = null;

        try {
            // Send the request; the response return in HttpResponse object
            response = httpGetClient.execute(request);
        } catch (Exception e) {
            // Close the connection
            httpGetClient.getConnectionManager().shutdown();
            e.printStackTrace();
        } finally {
            // ...
        }

        // verify the valid error code
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            // Close the connection
            httpGetClient.getConnectionManager().shutdown();
            throw new RuntimeException(targetUrl + " failed with HTTP error code : " + statusCode);
        }
        // pull the response object
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null) {
            return (httpEntity);
        } else {
            // Close the connection
            httpGetClient.getConnectionManager().shutdown();
            throw new Exception("httpEntity == null");
        }
    }

    private void putRequest(String targetUrl) throws Exception {

        DefaultHttpClient httpPutClient = new DefaultHttpClient();
        final HttpPut request = new HttpPut(baseUrl + targetUrl);
        HttpResponse response = null;

        try {
            // Send the request; the response return in HttpResponse object
            response = httpPutClient.execute(request);
        } catch (Exception e) {
            // Close the connection
            httpPutClient.getConnectionManager().shutdown();
            e.printStackTrace();
        } finally {
            // ...
        }

        // verify the valid error code
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            // Close the connection
            httpPutClient.getConnectionManager().shutdown();
            throw new RuntimeException(targetUrl + " failed with HTTP error code : " + statusCode);
        }
        // Close the connection
        httpPutClient.getConnectionManager().shutdown();
    }

    private boolean hasNextStep() {

        // Set the API media type in http accept header
        // getRequest.addHeader("accept", "application/json"); - generates 406 error

        String targetUrl = "hasNext";
        String strResponse = "{}";
        try {
            HttpEntity httpEntity = getRequest(targetUrl);
            strResponse = EntityUtils.toString(httpEntity);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /**
         * response format: {"result":"ok","hasNext":"true"}
         */
        JSONObject jsonResponse = new JSONObject(strResponse);
        return (Boolean.parseBoolean((String) jsonResponse.get("hasNext")));

        // JSONObject oj = tokenList.getJSONObject(0);
        // String token = oj.getString("name");
    }

    private String getNextStep() {

        String targetUrl = "getNext";
        String strResponse = "";
        try {
            HttpEntity httpEntity = getRequest(targetUrl);
            strResponse = EntityUtils.toString(httpEntity);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /**
         * response format: { "currentElementName": "e_AddNode", "result": "ok" }
         */
        JSONObject jsonResponse = new JSONObject(strResponse);
        return ((String) jsonResponse.get("currentElementName"));
    }


    /** >>>>>>>>  TESTS   <<<<<<<<<<< */


    // **************************************************************
    // ****     run GraphWalker online test as REST service      ****
    // **************************************************************

    // # in another terminal/PuTTy , lunch the GraphWalker REST service and load the model:

    // Execute
    // # If required, make sure to use the correct java:
    // $ export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
    // $ cd workspace/GraphWalker-rest_client/
    // $ java -jar ../lib/graphwalker-cli-4.2.0.jar -d all online -s RESTFUL -m src/test/resources/com/cyberark/BstModel.json "random(edge_coverage(100))"
    // OR:
    // $ java -jar ../lib/graphwalker-cli-4.2.0.jar -d all online -s RESTFUL -m src/test/resources/com/cyberark/BstModel.json "random(edge_coverage(100) && vertex_coverage(100))"
    // OR:
    // $ java -jar ../lib/graphwalker-cli-4.2.0.jar -d all online -s RESTFUL -m src/test/resources/com/cyberark/BstModel.json "random(time_duration(30))"

    // in VS-Code run:
    // -----------------
    // Execute
    // # If required, make sure to use the correct java:
    // $ export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
    // $ mvn clean compile test site jacoco:prepare-agent jacoco:report



    @BeforeEach
    public void initTest(){
        // make sure to restart the model execution
        try {
            new BstTest().putRequest("restart");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    @Test
    public void runFunctionalTest() {

        BstTest app = new BstTest();
        Method method = null;
        String nextStep;

        while(app.hasNextStep()) {
            nextStep = app.getNextStep();
            // System.out.println(nextStep);
            try {
                method = app.getClass().getMethod(nextStep);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            try {
                method.invoke(app);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }
}
