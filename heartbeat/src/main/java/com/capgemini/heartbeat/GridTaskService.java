package com.capgemini.heartbeat;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import org.openqa.selenium.WebDriverException;

public class GridTaskService implements TaskService {
	List<Property> gridList;
	private ResultCollector resultCollector;
	protected GridConnectionTester connection;

	public GridTaskService(TaskProperties gridPrperties) {
		this.gridList = gridPrperties.getPropertiesList();
		resultCollector = new ResultCollector();
		connection = new GridConnectionTester();
	}

	public ResultCollector getTasksResult() {
		this.resultCollector.flush();
		runTestsAndPrepareResults();
		return resultCollector;
	}

	private void runTestsAndPrepareResults() {
		HeartbeatFlow.log.info("Checking Selenium Grid...");

		for (Property hub : this.gridList) {
			boolean hubStatus = checkHub(hub);
			if (hubStatus) {
				checkNodes((GridProperty) hub);
			}
		}
	}

	private boolean checkHub(Property hub) {
		HeartbeatFlow.log.info("Test " + "SELENIUM HUB - " + hub.getUrl());
		boolean hubStatus = false;
		try {
			hubStatus = connection.hubConnectionTest(hub);
		} catch(ConnectException e){
			HeartbeatFlow.log.severe("Hub TEST FAILED with " + e.getClass().getName());
		}catch (IOException e) {
			HeartbeatFlow.log.severe("Hub TEST FAILED with " + e.getClass().getName());
		}
		String name = "SELENIUM HUB - " + hub.getUrl();
		Long timestamp = new Date().getTime();
		resultCollector.addResult(new TaskResult(timestamp, name, hubStatus ? "PING SUCCESS" : "PING FAILED"));
		return hubStatus;

	}

	private void checkNodes(GridProperty hub) {

		for (GridNode node : hub.getNodesList()) {
			String nodename = "SELENIUM Node - " + hub.getUrl() + " " + node.getBrowser() + " v."
					+ node.getBrowserVersion();
			HeartbeatFlow.log.info("Test " + nodename);
			try {

				String result = connection.nodeConnectionTest(hub, node);
				Long timestamp = new Date().getTime();
				resultCollector.addResult(new TaskResult(timestamp, nodename, result));

			} catch (WebDriverException e) {
				e.printStackTrace();
				Long timestamp = new Date().getTime();
				resultCollector
						.addResult(new TaskResult(timestamp, nodename, "TEST FAILED with " + e.getClass().getName()));
				HeartbeatFlow.log.severe("Node TEST FAILED with " + e.getClass().getName());

			} catch (MalformedURLException e) {
				e.printStackTrace();
				Long timestamp = new Date().getTime();
				resultCollector.addResult(new TaskResult(timestamp, nodename, "TEST FAILED Problem with HUB URL"));
				HeartbeatFlow.log.severe("Node TEST FAILED with " + e.getClass().getName());
			}
		}

	}

}
