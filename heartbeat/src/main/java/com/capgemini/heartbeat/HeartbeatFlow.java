package com.capgemini.heartbeat;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class HeartbeatFlow {
	private static final Logger log = Logger.getLogger(HeartbeatFlow.class.getName());

	private static void delayNextCheck(HeartbeatProperties hp) {
		try {
			log.info("Next check in: " + hp.getTime().Hours + "h " + hp.getTime().Minutes + "min "
					+ hp.getTime().Seconds + "s");
			TimeUnit.SECONDS.sleep(hp.getTime().Seconds);
			TimeUnit.MINUTES.sleep(hp.getTime().Minutes);
			TimeUnit.HOURS.sleep(hp.getTime().Hours);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {

		HeartbeatProperties hp = new HeartbeatPropertiesManager("./heartbeat.properties").getProperties();
		CSVReportCreator csvReportCreator = new CSVReportCreator(hp.getCsvReportPath());
		csvReportCreator.createBackupFile();

		TaskProperties jenkinsPrperties = new JenkinsProperties(hp.getJenkinsPropertiesPath());
		TaskProperties gridPrperties = new GridProperties(hp.getGridPrpertiesPath());
		TaskService jenkinsService = new JenkinsTaskService(jenkinsPrperties);
		TaskService gridService = new GridTaskService(gridPrperties);

		ResultCollector resultCollector = new ResultCollector();

		while (true) {
			resultCollector.flush();
			resultCollector.addResults(jenkinsService.getTasksResult());
			resultCollector.addResults(gridService.getTasksResult());

			csvReportCreator.updateReportWith(resultCollector.getTasksResult());

			delayNextCheck(hp);
		}

	}

}
