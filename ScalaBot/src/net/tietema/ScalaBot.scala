package net.tietema

import robocode._
import java.awt.{Graphics2D, Color}
import scala.collection.mutable
import robocode.util.Utils
import java.awt.geom.Point2D

/**
 * @author jeroen
 */
class ScalaBot extends AdvancedRobot {

  var maxX = 0.0
  var maxY = 0.0
  val CORNER_RADIUS = 160
  var currentTarget = ""

  var enemies = new mutable.HashMap[String, Enemy]

  override def run() {
    setAllColors(Color.RED)
    maxX = getBattleFieldWidth
    maxY = getBattleFieldHeight
    System.out.println("Battlefield width: " + maxX)
    System.out.println("Battlefield height: " + maxY)
    turnRight(getHeading % 90)

    while (true) {
      setAhead(185)
      setTurnRadarLeft(360)
      needToTurn()
      targetAndShootClosestEnemy()
      execute()
    }
  }

  def needToTurn() {
    var turnDegrees = 0.0
    if (getY <= CORNER_RADIUS && getX >= (maxX - CORNER_RADIUS)) {
      System.out.println("Hit bottom right")
      turnDegrees = 270 - getHeading
    }
    if (getX  >= (maxX - CORNER_RADIUS) && getY >= (maxY - CORNER_RADIUS)) {
      System.out.println("Hit top right")
      turnDegrees = 180 - getHeading
    }
    if (getY >= (maxY - CORNER_RADIUS) && getX <= CORNER_RADIUS) {
      System.out.println("Hit top left")
      turnDegrees = 90 - getHeading
    }
    if (getX <= CORNER_RADIUS && getY <= CORNER_RADIUS) {
      System.out.println("Hit bottom left")
      turnDegrees = 360 - getHeading
    }
    if (turnDegrees != 0.0) {
      System.out.println("Turning right (" + turnDegrees + ")")
      setTurnRight(turnDegrees)
    }
  }

  def targetAndShootClosestEnemy() {
    if (currentTarget.isEmpty) {
      currentTarget = findClosestTarget()
    }
    if (currentTarget.isEmpty) return

    val target = enemies.get(currentTarget)
    if (target.isEmpty) return

    val enemy = target.get
    val theta = Utils.normalAbsoluteAngle( Math.atan2( enemy.getX - getX, enemy.getY - getY ) )
    setTurnGunRightRadians( Utils.normalRelativeAngle( theta - getGunHeadingRadians ) )
    setFire( 2.0 )
  }

  def findClosestTarget(): String = {
    var closestDistance = 10000.0
    var closestEnemy = ""
    for ((name, enemy) <- enemies) {
      val distance = Point2D.distance(getX, getY, enemy.getX, enemy.getY)
      if (distance < closestDistance) {
        closestDistance = distance
        closestEnemy = name
      }
    }
    closestEnemy
  }


  def getHeadingBetween(heading1: Double, heading2: Double): Boolean = {
    getHeading > heading1 && getHeading < heading2
  }

  override def onHitWall(event: HitWallEvent) {
    System.out.println("onHitWall")
    val turn = 90 - Math.abs(Math.min(0, event.getBearing))
    System.out.println(turn)
    setTurnRight(turn)
  }


  override def onHitRobot(event: HitRobotEvent) {
    System.out.println("onHitRobot")
    setTurnRight(90)
  }

  override def onStatus(event: StatusEvent) {
  }

  override def onScannedRobot(event: ScannedRobotEvent) {
    val (enemyX, enemyY) = calculateXY(getX, getY, event.getBearing, event.getDistance)
    enemies.put(event.getName, new Enemy(enemyX, enemyY))
  }

  override def onRobotDeath(event: RobotDeathEvent) {
    enemies.remove(event.getName)
  }

  def calculateXY(x: Double, y: Double, bearing: Double, distance: Double): (Double, Double) = {
    // Calculate the angle to the scanned robot
    val angle = Math.toRadians((getHeading + bearing) % 360)

    // Calculate the coordinates of the robot
    val lastScannedX = ( getX + Math.sin( angle ) * distance )
    val lastScannedY = ( getY + Math.cos( angle ) * distance )

    (lastScannedX, lastScannedY)
  }

  override def onPaint(g: Graphics2D) {
    g.setColor(Color.GREEN)
    for ((name, enemy) <- enemies) {
      g.drawOval((enemy.getX - 10).toInt, (enemy.getY + 10).toInt,
        20, 20)
    }
    g.drawRect(0, 0, CORNER_RADIUS, CORNER_RADIUS)
    g.drawRect((maxX - CORNER_RADIUS).toInt, 0, CORNER_RADIUS, CORNER_RADIUS)
    g.drawRect((maxX - CORNER_RADIUS).toInt, (maxY - CORNER_RADIUS).toInt, CORNER_RADIUS, CORNER_RADIUS)
    g.drawRect(0, (maxY - CORNER_RADIUS).toInt, CORNER_RADIUS, CORNER_RADIUS)
  }
}
