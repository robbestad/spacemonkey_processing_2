import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.ArrayList; 
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileNotFoundException; 
import java.io.IOException; 
import java.io.BufferedInputStream; 
import java.net.MalformedURLException; 
import java.net.URL; 
import javax.sound.sampled.AudioFormat; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.DataLine; 
import javax.sound.sampled.LineUnavailableException; 
import javax.sound.sampled.SourceDataLine; 
import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class SpaceMonkeyDesktop extends PApplet {


public void setup()
{
  size(640,480);
  noStroke();
  noSmooth();
  
  maxim = new Maxim(this);
 //musicPlayer = maxim.loadFile("standoff.wav");
 //  musicPlayer.setLooping(true);
 // musicPlayer.volume(0.8);
 
  sfxLaser=maxim.loadFile("laser.wav");
  sfxLaser.setLooping(false);
 
  sfxExplode=maxim.loadFile("explode2.wav");
  sfxExplode.setLooping(false);
  
  sfxPickup=maxim.loadFile("pickup.wav");
  sfxPickup.setLooping(false);

  /*
  //debug
  sfxPickup.volume(0.0);
  sfxExplode.volume(0.0);
  sfxLaser.volume(0.0);
  */

  bgdead = loadImage("bgdead.png");
  bgstart = loadImage("bgstart.png");
  dust = loadImage("bg_iphone_parallax_spacedust.png");
  stars = loadImage("bg_iphone_parallax_stars.png");
  pos_dust = new PVector(0, 0);
  pos_stars = new PVector(4, 0);
  direction="down";
  if(direction=="down"){
   vel_dust = new PVector(0,-1); 
   vel_stars = new PVector(0,-1);
  } else {
   vel_dust = new PVector(0,1); 
   vel_stars = new PVector(0,1);
  }  

  //Scale the game so it is entirely on the screen
  if(game_w > game_h)
    game_s = (float)width / (float)game_w;
  else
    game_s = (float)height / (float)game_h;

  projectileList = new ArrayList<Projectile>();
  enemyList = new ArrayList<Enemy>();
  asteroidList = new ArrayList<Asteroid>();

  bananaList = new ArrayList<Banana>();
  starsList = new ArrayList<Stars>();
  explosionList = new ArrayList<Explosion>();
  dustList = new ArrayList<Dusthit>();
  

  healthBar = new Sprites();
  healthBar.x = health_x;
  healthBar.y = health_y;
  healthBar.h = health_h*2;
  healthBar.drawMode = CORNER;
  healthBar.c = health_c;

  monkey = new Monkey("monkey",4,51,84);
  monkey.x = player_x;
  monkey.y = player_y - (int)(10/game_s);
  monkey.w = player_w;
  monkey.h = player_h;
  monkey.drawMode = CENTER;
  monkey.c = player_c;

  frameRate(fr);
  background(bg);
}

public void draw()
{
  background(255);
  
  if(!game_started){
    background(bgstart);
    rectMode(CORNER);
      fill(button_over_color);
      if (mousePressed || fire) {
        // RESET IMPORTANT VARIABLES
        seconds=0;
        health = 100;
        enemyList.clear();
        killCount = 0;
        delaySpawn = true;
        delayBeforeSpawn_t0 = delayBeforeSpawn;
        monkey.x = player_x;
        monkey.y = player_y;
        game_started=true;
      }
    else 
    fill(button_idle_color);    
    rect(button_x0, startbox_y1, button_w, button_h);
    fill(0xff000000);
    textAlign(CENTER);
    text("Welcome to SPACE MONKEY", button_x0-10, startbox_y1+4, button_w, button_h);
    text("Move with [arrow] keys", button_x0-5, startbox_y1+18, button_w, button_h);
    text("[space] to shoot", button_x0-5, startbox_y1+32, button_w, button_h);
    text("Click here to play", button_x0-5, startbox_y1+48, button_w, button_h);
    
    
  } else
  if (health > 0 && game_started) {
 
  // update game timer
  frTimer+=1;
  if(frTimer==fr){
  seconds+=1;
  frTimer=0;
  }
  
  // make the game progressively harder
  if(seconds>10){
    enemy_maxCount = 2;
    asteroids_maxCount = 4;
  }
  if(seconds>25){
    enemy_maxCount = 3;
    asteroids_maxCount = 4;
    enemy_spawnDelay=200;
  }
  if(seconds>65){
    enemy_maxCount = 4;
    asteroids_maxCount = 6;
    enemy_spawnDelay=100;
  }
  if(seconds>120){
    enemy_maxCount = 6;
  }
  if(seconds>180){
    enemy_maxCount = 8;
    asteroids_maxCount = 8;
  }
  
    
  //musicPlayer.play();
  pushMatrix();
   parallax(stars, pos_stars, vel_stars, direction);
   parallax(dust, pos_dust, vel_dust, direction);
  popMatrix();

   //Record button states
  getButtonStates();

  // Act on latest recorded button states (smooth movement guaranteed)
  if (up) monkey.y -= player_v_y;
  if (down) monkey.y += player_v_y;
  if (left) monkey.x -= player_v_x;
  if (right) monkey.x += player_v_x;

  // Prevent monkey from exiting screen x-axis
  if (monkey.x < player_x_min)
    monkey.x = player_x_min;
  if (monkey.x > player_x_max)
    monkey.x = player_x_max;

  // Prevent  monkey from exiting screen y-axis
  if (monkey.y < player_y_min)
    monkey.y = player_y_min;
  if (monkey.y > player_y_max)
    monkey.y = player_y_max;

  // LASER SHOTS
  if (savedFire && !lastSavedFire && health > 0) {
    sfxLaser.play();
    Projectile newProj = new Projectile("laser1",1,10,41);
    newProj.x = monkey.x+20;
    newProj.y = monkey.y - monkey.h/2;
    newProj.w = player_shot_w;
    newProj.h = player_shot_h;
    newProj.v_x = player_shot_v_x;
    newProj.v_y = player_shot_v_y;
    newProj.drawMode = CENTER;
    projectileList.add(newProj);
  }
  
  
  // width distribution
  int[] widthdist = { 125,175,225,275,325,375,425,475,525 };
  shuffle(widthdist);
  int arraynum=widthdist.length-1;
  int randomx=0;
  
  // Generate bananas
  if (delaySpawn) {
    if (delayBeforeSpawn_t0 < 0)
      delaySpawn = false;
    else
      delayBeforeSpawn_t0 -= 1000/fr;
  } 
  else 
  if (bananaList.size() < 1) {
    enemy_lastSpawnTime = enemy_spawnDelay;
        randomx=widthdist[arraynum--];
      
        Banana newBanana = new Banana("banana");
        newBanana.y = -enemy_h;
        newBanana.h = enemy_h;
        newBanana.w = enemy_w;
        newBanana.c = enemy_c;
        newBanana.drawMode = CENTER;
        newBanana.x_0 = randomx;
        newBanana.v_x_amp = enemy_v_x_amp;
        newBanana.v_x_timestep = enemy_v_x_timestep;
        newBanana.tx = random(0, TWO_PI);
        newBanana.v_y = enemy_v_y;
        bananaList.add(newBanana);
  }
  
  
  
  // Generate stars
  if (delaySpawn) {
    if (delayBeforeSpawn_t0 < 0)
      delaySpawn = false;
    else
      delayBeforeSpawn_t0 -= 1000/fr;
  } 
  else 
  if (starsList.size() < 1) {
  float starType= random(0,1);
  String starpng = "star1";
  if(starType<0.2f){ starpng = "star1"; }
  else if(starType<0.4f){ starpng = "star2"; }
  else if(starType<0.6f){ starpng = "star3"; }
  else if(starType<0.8f){ starpng = "star4"; }
  else if(starType<1.0f){ starpng = "star5"; }
    

    enemy_lastSpawnTime = enemy_spawnDelay;
        Stars newStar = new Stars(starpng);
        newStar.y = -enemy_h;
        newStar.h = enemy_h;
        newStar.w = enemy_w;
        newStar.c = enemy_c;
        newStar.drawMode = CENTER;
        newStar.x_0 = widthdist[arraynum--];
        newStar.v_x_amp = enemy_v_x_amp;
        newStar.v_x_timestep = enemy_v_x_timestep;
        newStar.tx = random(0, TWO_PI);
        newStar.v_y = star_v_y+random(0.1f,0.9f);
        starsList.add(newStar);
    
  }
  
  // Generate rocks
  if (delaySpawn) {
    if (delayBeforeSpawn_t0 < 0)
      delaySpawn = false;
    else
      delayBeforeSpawn_t0 -= 1000/fr;
  } 
  else
    if (asteroidList.size() < asteroids_maxCount) {
      if (enemy_lastSpawnTime < 0) {
        enemy_lastSpawnTime = enemy_spawnDelay;
        Asteroid newAsteroid = new Asteroid("asteroid1");
        newAsteroid.drawMode = CENTER;
        newAsteroid.y = -enemy_h*2;
        newAsteroid.x_0 = widthdist[arraynum--];
        newAsteroid.v_x_amp = enemy_v_x_amp;
        newAsteroid.v_x_timestep = enemy_v_x_timestep;
        newAsteroid.tx = random(0, TWO_PI);
        newAsteroid.v_y = 2;
        asteroidList.add(newAsteroid);
        
        enemy_lastSpawnTime = enemy_spawnDelay;
        Asteroid newAsteroid1 = new Asteroid("asteroid");
        newAsteroid1.drawMode = CENTER;
        newAsteroid1.y = -enemy_h*2;
        
        newAsteroid1.x_0 = widthdist[arraynum--];
        newAsteroid1.v_x_amp = enemy_v_x_amp;
        newAsteroid.v_x_timestep = enemy_v_x_timestep;
        newAsteroid1.tx = random(0, TWO_PI);
        newAsteroid1.v_y = 3;
        asteroidList.add(newAsteroid1);
        }
      }
      else
        enemy_lastSpawnTime -= 1000/fr;
  
  
  //Generate enemies
  if (delaySpawn) {
    if (delayBeforeSpawn_t0 < 0)
      delaySpawn = false;
    else
      delayBeforeSpawn_t0 -= 1000/fr;
  } 
  else
    if (enemyList.size() < enemy_maxCount) {
      randomx=widthdist[arraynum--];
      if (enemy_lastSpawnTime < 0) {
        // Spawn two types of UFOs
        
        enemy_lastSpawnTime = enemy_spawnDelay;
        Enemy newEnemy = new Enemy("alien1");
        newEnemy.y = -enemy_h;
        newEnemy.h = enemy_h;
        newEnemy.w = enemy_w;
        newEnemy.c = enemy_c;
        newEnemy.drawMode = CORNER;
        newEnemy.x_0 = randomx;
        newEnemy.v_x_amp = enemy_v_x_amp;
        //newEnemy.v_x_timestep = enemy_v_x_timestep;
        newEnemy.v_x_timestep = HALF_PI/fr/2;
        newEnemy.tx = random(0, TWO_PI);
        newEnemy.v_y = enemy_v_y;
        enemyList.add(newEnemy);

        randomx=widthdist[arraynum--];
        enemy_lastSpawnTime = enemy_spawnDelay;
        Enemy newEnemy2 = new Enemy("alien2");
        newEnemy2.y = -enemy_h;
        newEnemy2.h = enemy_h;
        newEnemy2.w = enemy_w;
        newEnemy2.c = enemy_c;
        newEnemy2.drawMode = CORNER;
        newEnemy2.x_0 = randomx;
        newEnemy2.v_x_amp = enemy_v_x_amp;
        newEnemy2.v_x_timestep = HALF_PI/fr/4;
        newEnemy2.tx = random(0, TWO_PI);
        newEnemy2.v_y = enemy_v_y;
        enemyList.add(newEnemy2);
      
      }
      else
        enemy_lastSpawnTime -= 1000/fr;
    }

  
  for (int i = 0; i < bananaList.size(); i++) {
    Banana banana = bananaList.get(i);
    if (checkHit(banana, monkey)) {
      if (health > 0) {
        sfxPickup.play();
        health += bonusHealth;
        if (health > 100) health = 100;
        bananaList.remove(i--);
        continue;
      }
    }
  }
  for (int i = 0; i < asteroidList.size(); i++) {
    Asteroid asteroid = asteroidList.get(i);
    if (checkHit(asteroid, monkey)) {
      if (health > 0) {
        Explosion newExplosion = new Explosion("explosion",16,64,64,monkey.x,monkey.y-50);
        explosionList.add(newExplosion);
        health -= enemy_damage*1.3f;
        asteroidList.remove(i--);
        continue;
      }
    }
  }
  

  boolean skipcheck=false;
  for (int i = 0; i < enemyList.size(); i++) {
    Enemy enemy = enemyList.get(i);
    if (checkHit(enemy, monkey)) {
        Explosion newExplosion = new Explosion("explosion",16,64,64,monkey.x,monkey.y-15);
        explosionList.add(newExplosion);
      if (health > 0) {
        sfxExplode.play();
        health -= enemy_damage;
        enemyList.remove(i--);
        skipcheck=true;
        continue;
      }
    }
    for (int j = 0; j < projectileList.size(); j++) {
      Projectile proj = projectileList.get(j);
      if (checkHit(proj, enemy) && !skipcheck) {
        enemyList.remove(i--);
        projectileList.remove(j--);
        
        sfxExplode.play();
        Explosion newExplosion = new Explosion("explosion",16,64,64,proj.x,proj.y-50);
        explosionList.add(newExplosion);
        
        killCount+=250;
        
      }
    }
  }



      
   
   // check hit with asteorids
   for (int i = 0; i < asteroidList.size(); i++) {
    Asteroid asteroid = asteroidList.get(i);
    for (int j = 0; j < projectileList.size(); j++) {
      Projectile proj = projectileList.get(j);
      if (checkHit(proj, asteroid)) {
        //enemyList.remove(i--);
        projectileList.remove(j--);
        sfxExplode.play();
        Dusthit newHit = new Dusthit("dust",8,64,64,proj.x-20,proj.y-20);
        dustList.add(newHit);
        }
      }
    }

      
      
      
      


  //Move, display, and remove off-screen things
  for (int i = 0; i < projectileList.size(); i++) {
    Projectile proj = projectileList.get(i);
    proj.move();
    proj.disp();
    if (proj.y + proj.h < 0)
      projectileList.remove(i--);
  }

  
  for (int i = 0; i < asteroidList.size(); i++) {
    Asteroid asteroid = asteroidList.get(i);
    asteroid.move();
    asteroid.disp();
    if (asteroid.y - asteroid.h > game_h)
      asteroidList.remove(i--);
  }
  
   for (int i = 0; i < bananaList.size(); i++) {
    Banana banana = bananaList.get(i);
    banana.move();
    banana.disp();
    if (banana.y - banana.h > game_h)
      bananaList.remove(i--);
  }

  
   for (int i = 0; i < starsList.size(); i++) {
    Stars star = starsList.get(i);
    star.move();
    star.disp();
    if (star.y - star.h > game_h)
      starsList.remove(i--);
  }


  for (int i = 0; i < explosionList.size(); i++) {
    Explosion explosion = explosionList.get(i);
    explosion.disp();
    if(explosion.killed==1) explosionList.remove(i--);
  }
  
   for (int i = 0; i < dustList.size(); i++) {
    Dusthit dusthit = dustList.get(i);
    dusthit.disp();
    if(dusthit.killed==1) dustList.remove(i--);
  }


for (int i = 0; i < enemyList.size(); i++) {
    Enemy enemy = enemyList.get(i);
    enemy.move();
    enemy.disp();
    if (enemy.y - enemy.h > game_h)
      enemyList.remove(i--);
  }
  //Display the player and healthbar
  if (health > 0) {
    healthBar.w = (health*health_w_max)/health_max;
    healthBar.disp();
    monkey.disp();
  } 


  textAlign(LEFT);
  fill(0xff00FF00);
  text("Score: " + killCount, width/2-15, 13);
  
  noStroke();
  rectMode(CORNER);
  fill(game_padding);
  if(game_w > game_h)
    rect(0,game_h,width, height);
  else
    rect(game_w,0,width, height);
    
    
  }
  else {
    background(bgdead);
    rectMode(CORNER);
      fill(button_over_color);
      if (mousePressed) {
        // RESET IMPORTANT VARIABLES
        seconds=0;
        health = 100;
        enemyList.clear();
        killCount = 0;
        delaySpawn = true;
        delayBeforeSpawn_t0 = delayBeforeSpawn;
        monkey.x = player_x;
        monkey.y = player_y;
              
        // remove asteroids
        for (int i = 0; i < asteroidList.size(); i++) {
          asteroidList.remove(i--);
        }
        // remove ufos
        for (int i = 0; i < enemyList.size(); i++) {
          enemyList.remove(i--);
        }
        // remove ufos
        for (int i = 0; i < bananaList.size(); i++) {
          bananaList.remove(i--);
        }
        
      }
    else 
    fill(button_idle_color);    
    rect(button_x0, button_y0, button_w, button_h);
    fill(0xff000000);
    textAlign(CENTER);
    //killCount+=seconds*25;
    text("Oh no, you died...", button_x0, button_y0+2, button_w, button_h);
    text("... after only "+seconds+" seconds", button_x0, button_y0+18, button_w, button_h);
    
    text("Final score: "+killCount, button_x0, button_y0+33, button_w, button_h);
    text("Click here to try again", button_x0, button_y0+48, button_w, button_h);
    }

}

public void parallax(PImage img, PVector pos, PVector vel, String direction) {
 if(direction=="up"){
  if (pos.y> 0) image(img, 0, pos.y);
  if (pos.y+img.height < height) image(img, 0, pos.y+img.height);
  image(img, 0, pos.y);
  pos.sub(vel);
  if (pos.y+img.height < 0) pos.y += img.height;
  if (pos.y >= img.height) pos.y -= img.height;
 } else {
  if (pos.y> height) image(img, 0, pos.y);
  if (pos.y+img.height > 0) image(img, 0, pos.y-img.height);
  image(img, 0, pos.y);
  pos.sub(vel);
  if (pos.y+img.height < 0) pos.y += img.height;
  if (pos.y >= img.height) pos.y -= img.height;
 }   
 
 
}

public void changeSpeed() {
  float acc = .25f;
  if (mouseButton == LEFT){
  vel_dust.y -=1;
  vel_stars.y -=2;
  }else {
  vel_dust.y +=1;
  vel_stars.y +=2;
  }
 }


//Checks to see if the two sprites are overlapping
public boolean checkHit(Sprites a, Sprites b) {
  int a_x0, a_x1, a_y0, a_y1;
  int b_x0, b_x1, b_y0, b_y1;
  if (a.drawMode == CORNER) {
    a_x0 = a.x;
    a_x1 = a.x + a.w;
    a_y0 = a.y;
    a_y1 = a.y + a.h;
  } 
  else {
    a_x0 = a.x - a.w/2;
    a_x1 = a_x0 + a.w;
    a_y0 = a.y - a.h/2;
    a_y1 = a_y0 + a.h;
  }
  if (b.drawMode == CORNER) {
    b_x0 = b.x;
    b_x1 = b.x + b.w;
    b_y0 = b.y;
    b_y1 = b.y + b.h;
  } 
  else {
    b_x0 = b.x - b.w/2;
    b_x1 = b_x0 + b.w;
    b_y0 = b.y - b.h/2;
    b_y1 = b_y0 + b.h;
  }

  return (a_x1 >= b_x0) && (a_x0 <= b_x1) &&
    (a_y1 >= b_y0) && (a_y0 <= b_y1);
}

public void shuffle(int[] a)
{
  int temp;
  int pick;
  for(int i=0; i<a.length; i++)
  {
    temp = a[i];
    pick  = (int)random(5);
    a[i] = a[pick];
    a[pick]= temp;
  }
}
//The MIT License (MIT)

//Copyright (c) 2013 Mick Grierson, Matthew Yee-King, Marco Gillies

//Permission is hereby granted, free of charge, to any person obtaining a copy\u2028of this software and associated documentation files (the "Software"), to deal\u2028in the Software without restriction, including without limitation the rights\u2028to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\u2028copies of the Software, and to permit persons to whom the Software is\u2028furnished to do so, subject to the following conditions:
//The above copyright notice and this permission notice shall be included in\u2028all copies or substantial portions of the Software.

//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\u2028IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\u2028FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\u2028AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\u2028LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\u2028OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\u2028THE SOFTWARE.

















 
 
 
 

//import android.content.res.Resources;
// import android.app.Activity; 
// import android.os.Bundle; 
// import android.media.*;
// import android.media.audiofx.Visualizer;
// import android.content.res.AssetFileDescriptor;
// import android.hardware.*;


public class Maxim {

    private float sampleRate;

    public final float[] mtof = {
	0f, 8.661957f, 9.177024f, 9.722718f, 10.3f, 10.913383f, 11.562325f, 12.25f, 12.978271f, 13.75f, 14.567617f, 15.433853f, 16.351599f, 17.323914f, 18.354048f, 19.445436f, 20.601723f, 21.826765f, 23.124651f, 24.5f, 25.956543f, 27.5f, 29.135235f, 30.867706f, 32.703197f, 34.647827f, 36.708096f, 38.890873f, 41.203445f, 43.65353f, 46.249302f, 49.f, 51.913086f, 55.f, 58.27047f, 61.735413f, 65.406395f, 69.295654f, 73.416191f, 77.781746f, 82.406891f, 87.30706f, 92.498604f, 97.998856f, 103.826172f, 110.f, 116.540939f, 123.470825f, 130.81279f, 138.591309f, 146.832382f, 155.563492f, 164.813782f, 174.61412f, 184.997208f, 195.997711f, 207.652344f, 220.f, 233.081879f, 246.94165f, 261.62558f, 277.182617f, 293.664764f, 311.126984f, 329.627563f, 349.228241f, 369.994415f, 391.995422f, 415.304688f, 440.f, 466.163757f, 493.883301f, 523.25116f, 554.365234f, 587.329529f, 622.253967f, 659.255127f, 698.456482f, 739.988831f, 783.990845f, 830.609375f, 880.f, 932.327515f, 987.766602f, 1046.502319f, 1108.730469f, 1174.659058f, 1244.507935f, 1318.510254f, 1396.912964f, 1479.977661f, 1567.981689f, 1661.21875f, 1760.f, 1864.655029f, 1975.533203f, 2093.004639f, 2217.460938f, 2349.318115f, 2489.015869f, 2637.020508f, 2793.825928f, 2959.955322f, 3135.963379f, 3322.4375f, 3520.f, 3729.31f, 3951.066406f, 4186.009277f, 4434.921875f, 4698.63623f, 4978.031738f, 5274.041016f, 5587.651855f, 5919.910645f, 6271.926758f, 6644.875f, 7040.f, 7458.620117f, 7902.132812f, 8372.018555f, 8869.84375f, 9397.272461f, 9956.063477f, 10548.082031f, 11175.303711f, 11839.821289f, 12543.853516f, 13289.75f
    };

    private AudioThread audioThread;
    private PApplet processing;

    public Maxim (PApplet processing) {
	this.processing = processing;
	sampleRate = 44100f;
	audioThread = new AudioThread(sampleRate, 4096, false);
	audioThread.start();
	    
    }

    public float[] getPowerSpectrum() {
	return audioThread.getPowerSpectrum();
    }

    /** 
     *  load the sent file into an audio player and return it. Use
     *  this if your audio file is not too long want precision control
     *  over looping and play head position
     * @param String filename - the file to load
     * @return AudioPlayer - an audio player which can play the file
     */
    public AudioPlayer loadFile(String filename) {
	// this will load the complete audio file into memory
	AudioPlayer ap = new AudioPlayer(filename, sampleRate, processing);
	audioThread.addAudioGenerator(ap);
	// now we need to tell the audiothread
	// to ask the audioplayer for samples
	return ap;
    }

    /**
     * Create a wavetable player object with a wavetable of the sent
     * size. Small wavetables (<128) make for a 'nastier' sound!
     * 
     */
    public WavetableSynth createWavetableSynth(int size) {
	// this will load the complete audio file into memory
	WavetableSynth ap = new WavetableSynth(size, sampleRate);
	audioThread.addAudioGenerator(ap);
	// now we need to tell the audiothread
	// to ask the audioplayer for samples
	return ap;
    }
    // /**
    //  * Create an AudioStreamPlayer which can stream audio from the
    //  * internet as well as local files.  Does not provide precise
    //  * control over looping and playhead like AudioPlayer does.  Use this for
    //  * longer audio files and audio from the internet.
    //  */
    // public AudioStreamPlayer createAudioStreamPlayer(String url) {
    //     AudioStreamPlayer asp = new AudioStreamPlayer(url);
    //     return asp;
    // }
}




/**
 * This class can play audio files and includes an fx chain 
 */
public class AudioPlayer implements Synth, AudioGenerator {
    private FXChain fxChain;
    private boolean isPlaying;
    private boolean isLooping;
    private boolean analysing;
    private FFT fft;
    private int fftInd;
    private float[] fftFrame;
    private float[] powerSpectrum;

    //private float startTimeSecs;
    //private float speed;
    private int length;
    private short[] audioData;
    private float startPos;
    private float readHead;
    private float dReadHead;
    private float sampleRate;
    private float masterVolume;

    float x1, x2, y1, y2, x3, y3;

    public AudioPlayer(float sampleRate) {
	fxChain = new FXChain(sampleRate);
	this.sampleRate = sampleRate;
    }

    public AudioPlayer (String filename, float sampleRate, PApplet processing) {
	//super(filename);
	this(sampleRate);
	try {
	    // how long is the file in bytes?
	    //long byteCount = getAssets().openFd(filename).getLength();
	    File f = new File(processing.dataPath(filename));
	    long byteCount = f.length();
	    //System.out.println("bytes in "+filename+" "+byteCount);

	    // check the format of the audio file first!
	    // only accept mono 16 bit wavs
	    //InputStream is = getAssets().open(filename); 
	    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

	    // chop!!

	    int bitDepth;
	    int channels;
	    boolean isPCM;
	    // allows us to read up to 4 bytes at a time 
	    byte[] byteBuff = new byte[4];

	    // skip 20 bytes to get file format
	    // (1 byte)
	    bis.skip(20);
	    bis.read(byteBuff, 0, 2); // read 2 so we are at 22 now
	    isPCM = ((short)byteBuff[0]) == 1 ? true:false; 
	    //System.out.println("File isPCM "+isPCM);

	    // skip 22 bytes to get # channels
	    // (1 byte)
	    bis.read(byteBuff, 0, 2);// read 2 so we are at 24 now
	    channels = (short)byteBuff[0];
	    //System.out.println("#channels "+channels+" "+byteBuff[0]);
	    // skip 24 bytes to get sampleRate
	    // (32 bit int)
	    bis.read(byteBuff, 0, 4); // read 4 so now we are at 28
	    sampleRate = bytesToInt(byteBuff, 4);
	    //System.out.println("Sample rate "+sampleRate);
	    // skip 34 bytes to get bits per sample
	    // (1 byte)
	    bis.skip(6); // we were at 28...
	    bis.read(byteBuff, 0, 2);// read 2 so we are at 36 now
	    bitDepth = (short)byteBuff[0];
	    //System.out.println("bit depth "+bitDepth);
	    // convert to word count...
	    bitDepth /= 8;
	    // now start processing the raw data
	    // data starts at byte 36
	    int sampleCount = (int) ((byteCount - 36) / (bitDepth * channels));
	    audioData = new short[sampleCount];
	    int skip = (channels -1) * bitDepth;
	    int sample = 0;
	    // skip a few sample as it sounds like shit
	    bis.skip(bitDepth * 4);
	    while (bis.available () >= (bitDepth+skip)) {
		bis.read(byteBuff, 0, bitDepth);// read 2 so we are at 36 now
		//int val = bytesToInt(byteBuff, bitDepth);
		// resample to 16 bit by casting to a short
		audioData[sample] = (short) bytesToInt(byteBuff, bitDepth);
		bis.skip(skip);
		sample ++;
	    }

	    float secs = (float)sample / (float)sampleRate;
	    //System.out.println("Read "+sample+" samples expected "+sampleCount+" time "+secs+" secs ");      
	    bis.close();


	    // unchop
	    readHead = 0;
	    startPos = 0;
	    // default to 1 sample shift per tick
	    dReadHead = 1;
	    isPlaying = false;
	    isLooping = true;
	    masterVolume = 1;
	} 
	catch (FileNotFoundException e) {

	    e.printStackTrace();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void setAnalysing(boolean analysing_) {
	this.analysing = analysing_;
	if (analysing) {// initialise the fft
	    fft = new FFT();
	    fftInd = 0;
	    fftFrame = new float[1024];
	    powerSpectrum = new float[fftFrame.length/2];
	}
    }

    public float getAveragePower() {
	if (analysing) {
	    // calc the average
	    float sum = 0;
	    for (int i=0;i<powerSpectrum.length;i++){
		sum += powerSpectrum[i];
	    }
	    sum /= powerSpectrum.length;
	    return sum;
	}
	else {
	    System.out.println("call setAnalysing to enable power analysis");
	    return 0;
	}
    }
    public float[] getPowerSpectrum() {
	if (analysing) {
	    return powerSpectrum;
	}
	else {
	    System.out.println("call setAnalysing to enable power analysis");
	    return null;
	}
    }

    /** 
     *convert the sent byte array into an int. Assumes little endian byte ordering. 
     *@param bytes - the byte array containing the data
     *@param wordSizeBytes - the number of bytes to read from bytes array
     *@return int - the byte array as an int
     */
    private int bytesToInt(byte[] bytes, int wordSizeBytes) {
	int val = 0;
	for (int i=wordSizeBytes-1; i>=0; i--) {
	    val <<= 8;
	    val |= (int)bytes[i] & 0xFF;
	}
	return val;
    }

    /**
     * Test if this audioplayer is playing right now
     * @return true if it is playing, false otherwise
     */
    public boolean isPlaying() {
	return isPlaying;
    }

    /**
     * Set the loop mode for this audio player
     * @param looping 
     */
    public void setLooping(boolean looping) {
	isLooping = looping;
    }

    /**
     * Move the start pointer of the audio player to the sent time in ms
     * @param timeMs - the time in ms
     */
    public void cue(int timeMs) {
	//startPos = ((timeMs / 1000) * sampleRate) % audioData.length;
	//readHead = startPos;
	//System.out.println("AudioPlayer Cueing to "+timeMs);
	if (timeMs >= 0) {// ignore crazy values
	    readHead = (((float)timeMs / 1000f) * sampleRate) % audioData.length;
	    //System.out.println("Read head went to "+readHead);
	}
    }

    /**
     *  Set the playback speed,
     * @param speed - playback speed where 1 is normal speed, 2 is double speed
     */
    public void speed(float speed) {
	//System.out.println("setting speed to "+speed);
	dReadHead = speed;
    }

    /**
     * Set the master volume of the AudioPlayer
     */

    public void volume(float volume) {
	masterVolume = volume;
    }

    /**
     * Get the length of the audio file in samples
     * @return int - the  length of the audio file in samples
     */
    public int getLength() {
	return audioData.length;
    }
    /**
     * Get the length of the sound in ms, suitable for sending to 'cue'
     */
    public float getLengthMs() {
	return ((float) audioData.length / sampleRate * 1000f);
    }

    /**
     * Start playing the sound. 
     */
    public void play() {
	isPlaying = true;
    }

    /**
     * Stop playing the sound
     */
    public void stop() {
	isPlaying = false;
    }

    /**
     * implementation of the AudioGenerator interface
     */
    public short getSample() {
	if (!isPlaying) {
	    return 0;
	}
	else {
	    short sample;
	    readHead += dReadHead;
	    if (readHead > (audioData.length - 1)) {// got to the end
		//% (float)audioData.length;
		if (isLooping) {// back to the start for loop mode
		    readHead = readHead % (float)audioData.length;
		}
		else {
		    readHead = 0;
		    isPlaying = false;
		}
	    }

	    // linear interpolation here
	    // declaring these at the top...
	    // easy to understand version...
	    //      float x1, x2, y1, y2, x3, y3;
	    x1 = floor(readHead);
	    x2 = x1 + 1;
	    y1 = audioData[(int)x1];
	    y2 = audioData[(int) (x2 % audioData.length)];
	    x3 = readHead;
	    // calc 
	    y3 =  y1 + ((x3 - x1) * (y2 - y1));
	    y3 *= masterVolume;
	    sample = fxChain.getSample((short) y3);
	    if (analysing) {
		// accumulate samples for the fft
		fftFrame[fftInd] = (float)sample / 32768f;
		fftInd ++;
		if (fftInd == fftFrame.length - 1) {// got a frame
		    powerSpectrum = fft.process(fftFrame, true);
		    fftInd = 0;
		}
	    }

	    //return sample;
	    return (short)y3;
	}
    }

    public void setAudioData(short[] audioData) {
	this.audioData = audioData;
    }

    public short[] getAudioData() {
	return audioData;
    }

    public void setDReadHead(float dReadHead) {
	this.dReadHead = dReadHead;
    }

    ///
    //the synth interface
    // 

    public void ramp(float val, float timeMs) {
	fxChain.ramp(val, timeMs);
    } 



    public void setDelayTime(float delayMs) {
	fxChain.setDelayTime( delayMs);
    }

    public void setDelayFeedback(float fb) {
	fxChain.setDelayFeedback(fb);
    }

    public void setFilter(float cutoff, float resonance) {
	fxChain.setFilter( cutoff, resonance);
    }
}

/**
 * This class can play wavetables and includes an fx chain
 */
public class WavetableSynth extends AudioPlayer {

    private short[] sine;
    private short[] saw;
    private short[] wavetable;
    private float sampleRate;

    public WavetableSynth(int size, float sampleRate) {
	super(sampleRate);
	sine = new short[size];
	for (float i = 0; i < sine.length; i++) {
	    float phase;
	    phase = TWO_PI / size * i;
	    sine[(int)i] = (short) (sin(phase) * 32768);
	}
	saw = new short[size];
	for (float i = 0; i<saw.length; i++) {
	    saw[(int)i] = (short) (i / (float)saw.length *32768);
	}

	this.sampleRate = sampleRate;
	setAudioData(sine);
	setLooping(true);
    }

    public void setFrequency(float freq) {
	if (freq > 0) {
	    //System.out.println("freq freq "+freq);
	    setDReadHead((float)getAudioData().length / sampleRate * freq);
	}
    }

    public void loadWaveForm(float[] wavetable_) {
	if (wavetable == null || wavetable_.length != wavetable.length) {
	    // only reallocate if there is a change in length
	    wavetable = new short[wavetable_.length];
	}
	for (int i=0;i<wavetable.length;i++) {
	    wavetable[i] = (short) (wavetable_[i] * 32768);
	}
	setAudioData(wavetable);
    }
}

public interface Synth {
    public void volume(float volume);
    public void ramp(float val, float timeMs);  
    public void setDelayTime(float delayMs);  
    public void setDelayFeedback(float fb);  
    public void setFilter(float cutoff, float resonance);
    public void setAnalysing(boolean analysing);
    public float getAveragePower();
    public float[] getPowerSpectrum();
}

public class AudioThread extends Thread
{
    private int minSize;
    //private AudioTrack track;
    private short[] bufferS;
    private byte[] bOutput;
    private ArrayList audioGens;
    private boolean running;

    private FFT fft;
    private float[] fftFrame;
    private SourceDataLine sourceDataLine;
    private int blockSize;

    public AudioThread(float samplingRate, int blockSize) {
	this(samplingRate, blockSize, false);
    }

    public AudioThread(float samplingRate, int blockSize, boolean enableFFT)
    {
	this.blockSize = blockSize;
	audioGens = new ArrayList();
	// we'll do our dsp in shorts
	bufferS = new short[blockSize];
	// but we'll convert to bytes when sending to the sound card
	bOutput = new byte[blockSize * 2];
	AudioFormat audioFormat = new AudioFormat(samplingRate, 16, 1, true, false);
	DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
	    
	sourceDataLine = null;
	// here we try to initialise the audio system. try catch is exception handling, i.e. 
	// dealing with things not working as expected
	try {
	    sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
	    sourceDataLine.open(audioFormat, bOutput.length);
	    sourceDataLine.start();
	    running = true;
	} catch (LineUnavailableException lue) {
	    // it went wrong!
	    lue.printStackTrace(System.err);
	    System.out.println("Could not initialise audio. check above stack trace for more info");
	    //System.exit(1);
	}


	if (enableFFT) {
	    try {
		fft = new FFT();
	    }
	    catch(Exception e) {
		System.out.println("Error setting up the audio analyzer");
		e.printStackTrace();
	    }
	}
    }

    // overidden from Thread
    public void run() {
	running = true;
	while (running) {
	    //System.out.println("AudioThread : ags  "+audioGens.size());
	    for (int i=0;i<bufferS.length;i++) {
		// we add up using a 32bit int
		// to prevent clipping
		int val = 0;
		if (audioGens.size() > 0) {
		    for (int j=0;j<audioGens.size(); j++) {
			AudioGenerator ag = (AudioGenerator)audioGens.get(j);
			val += ag.getSample();
		    }
		    val /= audioGens.size();
		}
		bufferS[i] = (short) val;
	    }
	    // send it to the audio device!
	    sourceDataLine.write(shortsToBytes(bufferS, bOutput), 0, bOutput.length);
	}
    }
	
    public void addAudioGenerator(AudioGenerator ag) {
	audioGens.add(ag);
    }

    /**
     * converts an array of 16 bit samples to bytes
     * in little-endian (low-byte, high-byte) format.
     */
    private byte[] shortsToBytes(short[] sData, byte[] bData) {
	int index = 0;
	short sval;
	for (int i = 0; i < sData.length; i++) {
	    //short sval = (short) (fData[j][i] * ShortMaxValueAsFloat);
	    sval = sData[i];
	    bData[index++] = (byte) (sval & 0x00FF);
	    bData[index++] = (byte) ((sval & 0xFF00) >> 8);
	}
	return bData;
    }

    /**
     * Returns a recent snapshot of the power spectrum 
     */
    public float[] getPowerSpectrum() {
	// process the last buffer that was calculated
	if (fftFrame == null) {
	    fftFrame = new float[bufferS.length];
	}
	for (int i=0;i<fftFrame.length;i++) {
	    fftFrame[i] = ((float) bufferS[i] / 32768f);
	}
	return fft.process(fftFrame, true);
	//return powerSpectrum;
    }
}

/**
 * Implement this interface so the AudioThread can request samples from you
 */
public interface AudioGenerator {
    /** AudioThread calls this when it wants a sample */
    public short getSample();
}


public class FXChain  {
    private float currentAmp;
    private float dAmp;
    private float targetAmp;
    private boolean goingUp;
    private Filter filter;

    private float[] dLine;   

    private float sampleRate;

    public FXChain(float sampleRate_) {
	sampleRate = sampleRate_;
	currentAmp = 1;
	dAmp = 0;
	// filter = new MickFilter(sampleRate);
	filter = new RLPF(sampleRate);

	//filter.setFilter(0.1, 0.1);
    }

    public void ramp(float val, float timeMs) {
	// calc the dAmp;
	// - change per ms
	targetAmp = val;
	dAmp = (targetAmp - currentAmp) / (timeMs / 1000 * sampleRate);
	if (targetAmp > currentAmp) {
	    goingUp = true;
	}
	else {
	    goingUp = false;
	}
    }


    public void setDelayTime(float delayMs) {
    }

    public void setDelayFeedback(float fb) {
    }

    public void volume(float volume) {
    }


    public short getSample(short input) {
	float in;
	in = (float) input / 32768;// -1 to 1

	in =  filter.applyFilter(in);
	if (goingUp && currentAmp < targetAmp) {
	    currentAmp += dAmp;
	}
	else if (!goingUp && currentAmp > targetAmp) {
	    currentAmp += dAmp;
	}  

	if (currentAmp > 1) {
	    currentAmp = 1;
	}
	if (currentAmp < 0) {
	    currentAmp = 0;
	}  
	in *= currentAmp;  
	return (short) (in * 32768);
    }

    public void setFilter(float f, float r) {
	filter.setFilter(f, r);
    }
}


// /**
//  * Represents an audio source is streamed as opposed to being completely loaded (as WavSource is)
//  */
// public class AudioStreamPlayer {
// 	/** a class from the android API*/
// 	private MediaPlayer mediaPlayer;
// 	/** a class from the android API*/
// 	private Visualizer viz; 
// 	private byte[] waveformBuffer;
// 	private byte[] fftBuffer;
// 	private byte[] powerSpectrum;

// 	/**
// 	 * create a stream source from the sent url 
// 	 */
// 	public AudioStreamPlayer(String url) {
// 	    try {
// 		mediaPlayer = new MediaPlayer();
// 		//mp.setAuxEffectSendLevel(1);
// 		mediaPlayer.setLooping(true);

// 		// try to parse the URL... if that fails, we assume it
// 		// is a local file in the assets folder
// 		try {
// 		    URL uRL = new URL(url);
// 		    mediaPlayer.setDataSource(url);
// 		}
// 		catch (MalformedURLException eek) {
// 		    // couldn't parse the url, assume its a local file
// 		    AssetFileDescriptor afd = getAssets().openFd(url);
// 		    //mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
// 		    mediaPlayer.setDataSource(afd.getFileDescriptor());
// 		    afd.close();
// 		}

// 		mediaPlayer.prepare();
// 		//mediaPlayer.start();
// 		//System.out.println("Created audio with id "+mediaPlayer.getAudioSessionId());
// 		viz = new Visualizer(mediaPlayer.getAudioSessionId());
// 		viz.setEnabled(true);
// 		waveformBuffer = new byte[viz.getCaptureSize()];
// 		fftBuffer = new byte[viz.getCaptureSize()/2];
// 		powerSpectrum = new byte[viz.getCaptureSize()/2];
// 	    }
// 	    catch (Exception e) {
// 		System.out.println("StreamSource could not be initialised. Check url... "+url+ " and that you have added the permission INTERNET, RECORD_AUDIO and MODIFY_AUDIO_SETTINGS to the manifest,");
// 		e.printStackTrace();
// 	    }
// 	}

// 	public void play() {
// 	    mediaPlayer.start();
// 	}

// 	public int getLengthMs() {
// 	    return mediaPlayer.getDuration();
// 	}

// 	public void cue(float timeMs) {
// 	    if (timeMs >= 0 && timeMs < getLengthMs()) {// ignore crazy values
// 		mediaPlayer.seekTo((int)timeMs);
// 	    }
// 	}

// 	/**
// 	 * Returns a recent snapshot of the power spectrum as 8 bit values
// 	 */
// 	public byte[] getPowerSpectrum() {
// 	    // calculate the spectrum
// 	    viz.getFft(fftBuffer);
// 	    short real, imag;
// 	    for (int i=2;i<fftBuffer.length;i+=2) {
// 		real = (short) fftBuffer[i];
// 		imag = (short) fftBuffer[i+1];
// 		powerSpectrum[i/2] = (byte) ((real * real)  + (imag * imag));
// 	    }
// 	    return powerSpectrum;
// 	}

// 	/**
// 	 * Returns a recent snapshot of the waveform being played 
// 	 */
// 	public byte[] getWaveForm() {
// 	    // retrieve the waveform
// 	    viz.getWaveForm(waveformBuffer);
// 	    return waveformBuffer;
// 	}
// } 

/**
 * Use this class to retrieve data about the movement of the device
 */
public class Accelerometer  {
    //private SensorManager sensorManager;
    //private Sensor accelerometer;
    private float[] values;

    public Accelerometer() {
	//sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	//accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	//sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	values = new float[3];
	System.out.println("Java accelerometer will generate values of zero!");
    }

    public float[] getValues() {
	return values;
    }

    public float getX() {
	return values[0];
    }

    public float getY() {
	return values[1];
    }

    public float getZ() {
	return values[2];
    }

}

public interface Filter {
    public void setFilter(float f, float r);
    public float applyFilter(float in);
}

/** https://github.com/supercollider/supercollider/blob/master/server/plugins/FilterUGens.cpp */

public class RLPF implements Filter {
    float a0, b1, b2, y1, y2;
    float freq;
    float reson;
    float sampleRate;
    boolean changed;

    public RLPF(float sampleRate_) {
	this.sampleRate = sampleRate_;
	reset();
	this.setFilter(sampleRate / 4, 0.01f);
    }
    private void reset() {
	a0 = 0.f;
	b1 = 0.f;
	b2 = 0.f;
	y1 = 0.f;
	y2 = 0.f;
    }
    /** f is in the range 0-sampleRate/2 */
    public void setFilter(float f, float r) {
	// constrain 
	// limit to 0-1 
	f = constrain(f, 0, sampleRate/4);
	r = constrain(r, 0, 1);
	// invert so high r -> high resonance!
	r = 1-r;
	// remap to appropriate ranges
	f = map(f, 0f, sampleRate/4, 30f, sampleRate / 4);
	r = map(r, 0f, 1f, 0.005f, 2f);

	System.out.println("rlpf: f "+f+" r "+r);

	this.freq = f * TWO_PI / sampleRate;
	this.reson = r;
	changed = true;
    }

    public float applyFilter(float in) {
	float y0;
	if (changed) {
	    float D = tan(freq * reson * 0.5f);
	    float C = ((1.f-D)/(1.f+D));
	    float cosf = cos(freq);
	    b1 = (1.f + C) * cosf;
	    b2 = -C;
	    a0 = (1.f + C - b1) * .25f;
	    changed = false;
	}
	y0 = a0 * in + b1 * y1 + b2 * y2;
	y2 = y1;
	y1 = y0;
	if (Float.isNaN(y0)) {
	    reset();
	}
	return y0;
    }
}

/** https://github.com/micknoise/Maximilian/blob/master/maximilian.cpp */

class MickFilter implements Filter {

    private float f, res;
    private float cutoff, z, c, x, y, out;
    private float sampleRate;

    MickFilter(float sampleRate) {
	this.sampleRate = sampleRate;
    }

    public void setFilter(float f, float r) {
	f = constrain(f, 0, 1);
	res = constrain(r, 0, 1);
	f = map(f, 0, 1, 25, sampleRate / 4);
	r = map(r, 0, 1, 1, 25);
	this.f = f;
	this.res = r;    

	//System.out.println("mickF: f "+f+" r "+r);
    }
    public float applyFilter(float in) {
	return lores(in, f, res);
    }

    public float lores(float input, float cutoff1, float resonance) {
	//cutoff=cutoff1*0.5;
	//if (cutoff<10) cutoff=10;
	//if (cutoff>(sampleRate*0.5)) cutoff=(sampleRate*0.5);
	//if (resonance<1.) resonance = 1.;

	//if (resonance>2.4) resonance = 2.4;
	z=cos(TWO_PI*cutoff/sampleRate);
	c=2-2*z;
	float r=(sqrt(2.0f)*sqrt(-pow((z-1.0f), 3.0f))+resonance*(z-1))/(resonance*(z-1));
	x=x+(input-y)*c;
	y=y+x;
	x=x*r;
	out=y;
	return out;
    }
}


/*
 * This file is part of Beads. See http://www.beadsproject.net for all information.
 * CREDIT: This class uses portions of code taken from MPEG7AudioEnc. See readme/CREDITS.txt.
 */

/**
 * FFT performs a Fast Fourier Transform and forwards the complex data to any listeners. 
 * The complex data is a float of the form float[2][frameSize], with real and imaginary 
 * parts stored respectively.
 * 
 * @beads.category analysis
 */
public class FFT {

    /** The real part. */
    protected float[] fftReal;

    /** The imaginary part. */
    protected float[] fftImag;

    private float[] dataCopy = null;
    private float[][] features;
    private float[] powers;
    private int numFeatures;

    /**
     * Instantiates a new FFT.
     */
    public FFT() {
	features = new float[2][];
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    public float[] process(float[] data, boolean direction) {
	if (powers == null) powers = new float[data.length/2];
	if (dataCopy==null || dataCopy.length!=data.length)
	    dataCopy = new float[data.length];
	System.arraycopy(data, 0, dataCopy, 0, data.length);

	fft(dataCopy, dataCopy.length, direction);
	numFeatures = dataCopy.length;
	fftReal = calculateReal(dataCopy, dataCopy.length);
	fftImag = calculateImaginary(dataCopy, dataCopy.length);
	features[0] = fftReal;
	features[1] = fftImag;
	// now calc the powers
	return specToPowers(fftReal, fftImag, powers);
    }

    public float[] specToPowers(float[] real, float[] imag, float[] powers) {
	float re, im;
	double pow;
	for (int i=0;i<powers.length;i++) {
	    //real = spectrum[i][j].re();
	    //imag = spectrum[i][j].im();
	    re = real[i];
	    im = imag[i];
	    powers[i] = (re*re + im * im);
	    powers[i] = (float) Math.sqrt(powers[i]) / 10;
	    // convert to dB
	    pow = (double) powers[i];
	    powers[i] = (float)(10 *  Math.log10(pow * pow)); // (-100 - 100)
	    powers[i] = (powers[i] + 100) * 0.005f; // 0-1
	}
	return powers;
    }

    /**
     * The frequency corresponding to a specific bin 
     * 
     * @param samplingFrequency The Sampling Frequency of the AudioContext
     * @param blockSize The size of the block analysed
     * @param binNumber 
     */
    public  float binFrequency(float samplingFrequency, int blockSize, float binNumber)
    {    
	return binNumber*samplingFrequency/blockSize;
    }

    /**
     * Returns the average bin number corresponding to a particular frequency.
     * Note: This function returns a float. Take the Math.round() of the returned value to get an integral bin number. 
     * 
     * @param samplingFrequency The Sampling Frequency of the AudioContext
     * @param blockSize The size of the fft block
     * @param freq  The frequency
     */

    public  float binNumber(float samplingFrequency, int blockSize, float freq)
    {
	return blockSize*freq/samplingFrequency;
    }

    /** The nyquist frequency for this samplingFrequency 
     * 
     * @params samplingFrequency the sample
     */
    public  float nyquist(float samplingFrequency)
    {
	return samplingFrequency/2;
    }

    /*
     * All of the code below this line is taken from Holger Crysandt's MPEG7AudioEnc project.
     * See http://mpeg7audioenc.sourceforge.net/copyright.html for license and copyright.
     */

    /**
     * Gets the real part from the complex spectrum.
     * 
     * @param spectrum
     *            complex spectrum.
     * @param length 
     *       length of data to use.
     * 
     * @return real part of given length of complex spectrum.
     */
    protected  float[] calculateReal(float[] spectrum, int length) {
	float[] real = new float[length];
	real[0] = spectrum[0];
	real[real.length/2] = spectrum[1];
	for (int i=1, j=real.length-1; i<j; ++i, --j)
	    real[j] = real[i] = spectrum[2*i];
	return real;
    }

    /**
     * Gets the imaginary part from the complex spectrum.
     * 
     * @param spectrum
     *            complex spectrum.
     * @param length 
     *       length of data to use.
     * 
     * @return imaginary part of given length of complex spectrum.
     */
    protected  float[] calculateImaginary(float[] spectrum, int length) {
	float[] imag = new float[length];
	for (int i=1, j=imag.length-1; i<j; ++i, --j)
	    imag[i] = -(imag[j] = spectrum[2*i+1]);
	return imag;
    }

    /**
     * Perform FFT on data with given length, regular or inverse.
     * 
     * @param data the data
     * @param n the length
     * @param isign true for regular, false for inverse.
     */
    protected  void fft(float[] data, int n, boolean isign) {
	float c1 = 0.5f; 
	float c2, h1r, h1i, h2r, h2i;
	double wr, wi, wpr, wpi, wtemp;
	double theta = 3.141592653589793f/(n>>1);
	if (isign) {
	    c2 = -.5f;
	    four1(data, n>>1, true);
	} 
	else {
	    c2 = .5f;
	    theta = -theta;
	}
	wtemp = Math.sin(.5f*theta);
	wpr = -2.f*wtemp*wtemp;
	wpi = Math.sin(theta);
	wr = 1.f + wpr;
	wi = wpi;
	int np3 = n + 3;
	for (int i=2,imax = n >> 2, i1, i2, i3, i4; i <= imax; ++i) {
	    /** @TODO this can be optimized */
	    i4 = 1 + (i3 = np3 - (i2 = 1 + (i1 = i + i - 1)));
	    --i4; 
	    --i2; 
	    --i3; 
	    --i1; 
	    h1i =  c1*(data[i2] - data[i4]);
	    h2r = -c2*(data[i2] + data[i4]);
	    h1r =  c1*(data[i1] + data[i3]);
	    h2i =  c2*(data[i1] - data[i3]);
	    data[i1] = (float) ( h1r + wr*h2r - wi*h2i);
	    data[i2] = (float) ( h1i + wr*h2i + wi*h2r);
	    data[i3] = (float) ( h1r - wr*h2r + wi*h2i);
	    data[i4] = (float) (-h1i + wr*h2i + wi*h2r);
	    wr = (wtemp=wr)*wpr - wi*wpi + wr;
	    wi = wi*wpr + wtemp*wpi + wi;
	}
	if (isign) {
	    float tmp = data[0]; 
	    data[0] += data[1];
	    data[1] = tmp - data[1];
	} 
	else {
	    float tmp = data[0];
	    data[0] = c1 * (tmp + data[1]);
	    data[1] = c1 * (tmp - data[1]);
	    four1(data, n>>1, false);
	}
    }

    /**
     * four1 algorithm.
     * 
     * @param data
     *            the data.
     * @param nn
     *            the nn.
     * @param isign
     *            regular or inverse.
     */
    private  void four1(float data[], int nn, boolean isign) {
	int n, mmax, istep;
	double wtemp, wr, wpr, wpi, wi, theta;
	float tempr, tempi;

	n = nn << 1;        
	for (int i = 1, j = 1; i < n; i += 2) {
	    if (j > i) {
		// SWAP(data[j], data[i]);
		float swap = data[j-1];
		data[j-1] = data[i-1];
		data[i-1] = swap;
		// SWAP(data[j+1], data[i+1]);
		swap = data[j];
		data[j] = data[i]; 
		data[i] = swap;
	    }      
	    int m = n >> 1;
	    while (m >= 2 && j > m) {
		j -= m;
		m >>= 1;
	    }
	    j += m;
	}
	mmax = 2;
	while (n > mmax) {
	    istep = mmax << 1;
	    theta = 6.28318530717959f / mmax;
	    if (!isign)
		theta = -theta;
	    wtemp = Math.sin(0.5f * theta);
	    wpr = -2.0f * wtemp * wtemp;
	    wpi = Math.sin(theta);
	    wr = 1.0f;
	    wi = 0.0f;
	    for (int m = 1; m < mmax; m += 2) {
		for (int i = m; i <= n; i += istep) {
		    int j = i + mmax;
		    tempr = (float) (wr * data[j-1] - wi * data[j]);  
		    tempi = (float) (wr * data[j]   + wi * data[j-1]);  
		    data[j-1] = data[i-1] - tempr;
		    data[j]   = data[i] - tempi;
		    data[i-1] += tempr;
		    data[i]   += tempi;
		}
		wr = (wtemp = wr) * wpr - wi * wpi + wr;
		wi = wi * wpr + wtemp * wpi + wi;
	    }
	    mmax = istep;
	}
    }
}


boolean up = false;
boolean down = false;
boolean left = false;
boolean right = false;
boolean fire = false;

//Latched button states
boolean savedUp = false;
boolean savedDown = false;
boolean savedLeft = false;
boolean savedRight = false;
boolean savedFire = false;

//Last latched button states (for edge detection)
boolean lastSavedUp = false;
boolean lastSavedDown = false;
boolean lastSavedLeft = false;
boolean lastSavedRight = false;
boolean lastSavedFire = false;

public void keyPressed() {
  up = up || key == 'w' || key == 'W' || keyCode == UP;
  down = down || key == 's' || key == 'S' || keyCode == DOWN;
  left = left || key == 'a' || key == 'A' || keyCode == LEFT;
  right = right || key == 'd' || key == 'D' || keyCode == RIGHT;
  fire = fire || key == ' ';

}

public void keyReleased() {
  up = up && !(key == 'w' || key == 'W' || keyCode == UP);
  down = down && !(key == 's' || key == 'S' || keyCode == DOWN);
  left = left && !(key == 'a' || key == 'A' || keyCode == LEFT);
  right = right && !(key == 'd' || key == 'D' || keyCode == RIGHT);
  fire = fire && !(key == ' ');
}

public void getButtonStates() {
  //Record last retrieved state
  lastSavedUp = savedUp;
  lastSavedDown = savedDown;
  lastSavedLeft = savedLeft;
  lastSavedRight = savedRight;
  lastSavedFire = savedFire;
  //Get the next state
  savedUp = up;
  savedDown = down;
  savedLeft = left;
  savedRight = right;
  savedFire = fire;
}

//The MIT License (MIT)

//Copyright (c) 2013 Mick Grierson, Matthew Yee-King, Marco Gillies

//Permission is hereby granted, free of charge, to any person obtaining a copy\u2028of this software and associated documentation files (the "Software"), to deal\u2028in the Software without restriction, including without limitation the rights\u2028to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\u2028copies of the Software, and to permit persons to whom the Software is\u2028furnished to do so, subject to the following conditions:
//The above copyright notice and this permission notice shall be included in\u2028all copies or substantial portions of the Software.

//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\u2028IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\u2028FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\u2028AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\u2028LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\u2028OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN\u2028THE SOFTWARE.


public PImage [] loadImages(String stub, String extension, int numImages)
{
  PImage [] images = new PImage[0];
  for(int i =0; i < numImages; i++)
  {
    PImage img = loadImage(stub+i+extension);
    if(img != null)
    {
      images = (PImage [])append(images,img);
    }
    else
    {
      break;
    }
  }
  return images;
}
class Sprites {
  public int x = 0, y = 0;
  public int w = 0, h = 0;
  public int c = 0xffFFFFFF;
  public int drawMode = CORNER;
  public PImage [] spriteimg;  
  public int frames=1;
  public void disp() {
    rectMode(drawMode);
    fill(c);
    noStroke();
    rect(x, y, w, h);
  }
}

class Enemy extends Sprites {
  Enemy(String imgname){
    spriteimg = loadImages(imgname, ".png", 1);
  }
  
  public void disp(){
    int currentFrame=0;
   image(spriteimg[(int)currentFrame],x, y, 64, 64);
  }
  public int v_y = 0; //vertical velocity
  public float x_0 = 0f; //x offset for enemy's movement
  public float v_x_amp = 0f; //amplitude of enemy's x movement
  public float v_x_timestep = 0f; //used to move the enemy horizontally
  public float tx = 0f; //used to store the enemy's current position
  
  //Use the members to move the object
  public void move() {
    tx = (tx + v_x_timestep) % TWO_PI;
    x = (int)(x_0 + v_x_amp*sin(tx));
    y += v_y;
  }
}

//Another extension of Sprites
class Projectile extends Sprites {
  public int v_x = 0, v_y = 0;
  private int currentFrame=0;
  private int numFrames=0;

  Projectile(String imgname, int frames, int imwidth, int imheight){
    spriteimg = loadImages(imgname, ".png", frames);
    w=imwidth;
    h=imheight;
   numFrames=frames;
  }
 
  public void disp(){
   image(spriteimg[(int)currentFrame],x, y,w, h);
   currentFrame++;
   if(currentFrame>=numFrames)currentFrame=0;
  }
  
  //Use the members to move the object
  public void move() {
    this.x += this.v_x;
    this.y += this.v_y;
  }
}


// MONKEY (PLAYER)
class Monkey extends Sprites {
  private int currentFrame=0;
  private int numFrames=0;

  Monkey(String imgname, int frames, int imwidth, int imheight){
    spriteimg = loadImages(imgname, ".png", frames);
    w=imwidth;
    h=imheight;
   numFrames=frames;
  }
 
  public void disp(){
   image(spriteimg[(int)currentFrame],x, y,w, h);
   currentFrame++;
   if(currentFrame>=numFrames)currentFrame=0;
  }
  
  public int v_x = 0, v_y = 0;
  //Use the members to move the object
  public void move() {
    this.x += this.v_x;
    this.y += this.v_y;
  }
}

    
// DANGEROUS ROCKS
class Asteroid extends Sprites {
  Asteroid(String imgname){
    //spriteimg = loadImage(imgname);
    spriteimg = loadImages(imgname, ".png", 1);
  }
 
  public void disp(){
   int currentFrame=0;
   image(spriteimg[(int)currentFrame],x, y, 64, 60);
   w=64;
   h=60;
  }
  
  public int v_y = 0; //vertical velocity
  public float x_0 = 0f; //x offset for enemy's movement
  public float v_x_amp = 0f; //amplitude of enemy's x movement
  public float v_x_timestep = 0f; //used to move the enemy horizontally
  public float tx = 0f; //used to store the enemy's current position
  
  //Use the members to move the object
  public void move() {
    tx = (tx + v_x_timestep/4) % TWO_PI;
    x = (int)(x_0 + v_x_amp*cos(tx));
    y += v_y;
  }
}


// BANANA BONUS
class Banana extends Sprites {
  Banana(String imgname){
    spriteimg = loadImages(imgname, ".png", 1);
  }
 
  public void disp(){
    int currentFrame=0;
   image(spriteimg[(int)currentFrame],x, y, 64, 60);
  }
  
  public int v_y = 0; //vertical velocity
  public float x_0 = 0f; //x offset for enemy's movement
  public float v_x_amp = 0f; //amplitude of enemy's x movement
  public float v_x_timestep = 0f; //used to move the enemy horizontally
  public float tx = 0f; //used to store the enemy's current position
  
  //Use the members to move the object
  public void move() {
    tx = (tx + v_x_timestep) % TWO_PI;
    x = (int)(x_0/2 + v_x_amp*sin(tx));
    y += v_y*1.8f;
  }
}


// HARMLESS STARS
class Stars extends Sprites {
  Stars(String imgname){
    spriteimg = loadImages(imgname, ".png", 1);
  }
 
  public void disp(){
    int currentFrame=0;
   image(spriteimg[(int)currentFrame],x, y, 64, 60);
  }
  
  public float v_y = 0; //vertical velocity
  public float x_0 = 0f; //x offset for enemy's movement
  public float v_x_amp = 0f; //amplitude of enemy's x movement
  public float v_x_timestep = 0f; //used to move the enemy horizontally
  public float tx = 0f; //used to store the enemy's current position
  
  //Use the members to move the object
  public void move() {
    tx = 0;//(tx + v_x_timestep) % TWO_PI;
    x = (int)(x_0 + v_x_amp*sin(tx));
    y += v_y+v_x_timestep;
  }
}
    
    
    
// EXPLOSION
class Explosion extends Sprites {
  private int currentFrame=0;
  private int numFrames=0;
  
  public float killed = 0; //vertical velocity
 
  Explosion(String imgname, int frames, int imwidth, int imheight, int xpos, int ypos){
    spriteimg = loadImages(imgname, ".png", frames);
    w=imwidth;
    h=imheight;
    x=xpos;
    y=ypos;
   numFrames=frames;
  }
 
  public void disp(){
   image(spriteimg[(int)currentFrame],x, y,w, h);
   currentFrame++;
   if(currentFrame>=numFrames){currentFrame=0;killed=1;}
  }
  
}  


    
// DUST
class Dusthit extends Sprites {
  private int currentFrame=0;
  private int numFrames=0;
  
  public float killed = 0; //vertical velocity
 
  Dusthit(String imgname, int frames, int imwidth, int imheight, int xpos, int ypos){
    spriteimg = loadImages(imgname, ".png", frames);
    w=imwidth;
    h=imheight;
    x=xpos;
    y=ypos;
    numFrames=frames;
  }
 
  public void disp(){
   image(spriteimg[(int)currentFrame],x, y,w, h);
   currentFrame++;
   if(currentFrame>=numFrames){currentFrame=0;killed=1;}
  }
  
}  
// Music
Maxim maxim;
AudioPlayer musicPlayer;
AudioPlayer sfxLaser;
AudioPlayer sfxExplode;
AudioPlayer sfxPickup;

// first run
boolean game_started=false;

// Timer
int frTimer=0; // counts seconds based on framerate
int seconds=0; // updates by 1 foreach framerate run

// Background
PImage dust, stars, bgdead, bgstart;
PVector pos_dust, pos_stars;
PVector vel_dust, vel_stars;
String direction;

//Game properties
final int bg = 50;
final int game_w = 640; // # pixels wide in "game-space"
final int game_h = 480; // # pixels tall in "game-space"
float game_s; //games's scale value (used to move between game and
              //screen position values
final int game_padding = 0xff000000; //The color used when the aspect ratio
                                    //of the game differs from the screen
final int fr = 100; //framerate
int killCount = 0; //the score

//Player properties
final int player_w = 51;
final int player_h = 84;
final int player_x = game_w/2;
final int player_y = (8*game_h)/10; 
final int player_x_min = game_w/10;
final int player_x_max = (8*game_w)/10;
final int player_y_min = (2*game_h)/3;
final int player_y_max = (8*game_h)/10;
final int player_v_x = 3, player_v_y = 2;
final int player_c = 0xff00FFFF;

//Healthbar properties
final int health_w_max = game_w - 4;
final int health_h = 3;
final int health_x = 2;
final int health_y = game_h - health_h - 2;
final int health_max = 100;
final int health_c = 0xff0011FF;
int health = 100;

//Enemy properties
final int enemy_w = 64;
final int enemy_h = 64;
final float enemy_v_x_amp = 0.3f*(float)game_w;
final float enemy_v_x_timestep = HALF_PI/fr;
final int enemy_v_y = 1;
final float star_v_y = 1;

final int enemy_damage = 22;
final int enemy_c = 0xffFF0000;
final int bonusHealth = 27;

//Enemy spawner properties
int enemy_maxCount = 1;
int asteroids_maxCount = 2; // must be multiples of 2
int enemy_spawnDelay = 300;
final int delayBeforeSpawn = 1500;
int enemy_lastSpawnTime = 0;
int delayBeforeSpawn_t0 = delayBeforeSpawn;
boolean delaySpawn = true;

//Projectile properties
final int player_shot_w = 10, player_shot_h = 41;
final int player_shot_v_x = 0, player_shot_v_y = -6;

//Restart button properties
final int button_w = 200;
final int button_h = 64;
final int button_x0 = game_w/2 - button_w/2;
final int button_x1 = button_x0 + button_w;
final int button_y0 = game_h/2 - button_h/2 - 180;
final int button_y1 = button_y0 + button_h;
final int startbox_y1=button_y0+300;
final int button_idle_color = 0xff00FF00;
final int button_over_color = 0xffFFFFFF;

//Player and other entities
Monkey monkey;
Sprites healthBar;
Enemy enemy;
Asteroid asteroid;
Banana banana;
Stars star;
Explosion explosion;
Dusthit dusthit;

ArrayList<Projectile> projectileList;
ArrayList<Enemy> enemyList;
ArrayList<Asteroid> asteroidList;
ArrayList<Banana> bananaList;
ArrayList<Stars> starsList;
ArrayList<Explosion> explosionList;
ArrayList<Dusthit> dustList;

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "SpaceMonkeyDesktop" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
