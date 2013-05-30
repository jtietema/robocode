package net.tietema;

import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class DeathBot extends Robot {
	
	double lastBearing = 0.0;
	long lastseen = -4L;
	double firePower = 0.2;
	boolean searching = true;
	
	@Override
	public void run() {
		int noScanCount = 0;
		while (true) {
			turnRadarLeft(30);
			ahead(30);
			if (searching)
				turnGunRight(360);
			turnRadarRight(60);
			ahead(30);
			turnRadarLeft(30);
			if (getTime() - lastseen < 3)
				pointGunAtTarget(lastBearing);
			if (!searching)
				noScanCount++;
			if (noScanCount > 10)
				searching = true;
		}
	}
	
	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		back(5);
		turnLeft(5);
	}
	
	@Override
	public void onHitRobot(HitRobotEvent event) {
		turnLeft(45);
		turnGunRight(45);
		
		
	}
	
	@Override
	public void onHitWall(HitWallEvent event) {
		turnRight(45);
		turnGunLeft(45);
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		lastBearing = event.getBearing();
		lastseen = event.getTime();
		searching = false;
		pointGunAtTarget(event.getBearing());
		
		fire(firePower);
		ahead(15);
	}
	
	private void pointGunAtTarget(double bearing) {
		double newHeading = getHeading() + bearing;
		if (getGunHeading() > newHeading) // fix the rotating gun bug
			turnGunLeft(getGunHeading() - (newHeading % 360));
		else 
			turnGunRight((newHeading % 360) - getGunHeading());
	}
	
	private void pointRadarAtTarget(double bearing) {
		double newHeading = getHeading() + bearing % 360;
		if (getGunHeading() > newHeading)
			turnRadarLeft(getGunHeading() - newHeading);
		else 
			turnRadarRight(newHeading - getGunHeading());
	}
	
	@Override
	public void onStatus(StatusEvent e) {
	}
	
	@Override
	public void onBulletHit(BulletHitEvent event) {
		firePower = Math.min(firePower * 2, 3);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		firePower = Math.max(firePower / 2, 0.1);
	}
	
}
