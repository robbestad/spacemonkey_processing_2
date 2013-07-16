
void setup()
{
  size(320,480);
  noStroke();
  noSmooth();
  
  maxim = new Maxim(this);
  musicPlayer = maxim.loadFile("standoff.wav");
  
  musicPlayer.setLooping(true);
  musicPlayer.volume(0.8);
 
  sfxLaser=maxim.loadFile("laser.wav");
  sfxLaser.setLooping(false);
 
  sfxExplode=maxim.loadFile("explosion.wav");
  sfxExplode.setLooping(false);

  sfxExplodeSmall=maxim.loadFile("explosion_small.wav");
  sfxExplodeSmall.setLooping(false);
  
  sfxPickup=maxim.loadFile("pickup.wav");
  sfxPickup.setLooping(false);

  bgdead = loadImage("bgdeadm.png");
  bgstart = loadImage("bgstartm.png");
  
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
  enemyProjectileList = new ArrayList<EnemyProjectile>();
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

void draw()
{
  background(0);
  if(!game_started){
    image(bgstart);
     

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
    ellipse(button_x0+100, startbox_y1-5, button_w, button_h);
    fill(#000000);
    textAlign(CENTER);
    text("Move with [arrow] keys", button_x0, startbox_y1-23, button_w, button_h);
    text("[space] to shoot", button_x0, startbox_y1-9, button_w, button_h);
    text("Click here to play", button_x0, startbox_y1+7, button_w, button_h);
    
    
  } else
  if (health > 0 && game_started) {
  background(255);
  
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
  
  coolDown-=7;
    
  musicPlayer.play();
  pushMatrix();
   parallax(stars, pos_stars, vel_stars, direction);
   parallax(dust, pos_dust, vel_dust, direction);
  popMatrix();

   //Record button states
  getButtonStates();
  if(mousePressed){
   if(mouseX<  monkey.x) monkey.x -= player_v_x;
   else monkey.x += player_v_x;
   
   savedFire=true;
  }
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
    sfxLaser.stop();
    sfxLaser.cue(0);
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
  
  
  // shooot back
  if (coolDown<0) {
   if (enemyList.size() > 0 && enemyProjectileList .size() < 1) {
    for (int i = 0; i < enemyList.size(); i++) {
    Enemy enemy = enemyList.get(i);
    EnemyProjectile newProj = new EnemyProjectile("laser1",1,10,41);
    newProj.x = enemy.x+27;
    newProj.y = enemy.y;
    newProj.drawMode = CENTER;
    enemyProjectileList.add(newProj);
    coolDown=500;
    }
   }
  }
  // width distribution
  int[] widthdist = { 75,125, 175,225,275 };
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
  if(starType<0.2){ starpng = "star1"; }
  else if(starType<0.4){ starpng = "star2"; }
  else if(starType<0.6){ starpng = "star3"; }
  else if(starType<0.8){ starpng = "star4"; }
  else if(starType<1.0){ starpng = "star5"; }
    

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
        newStar.v_y = star_v_y+random(0.1,0.9);
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
        sfxPickup.stop();
        sfxPickup.cue(0);

        sfxPickup.play();
        health += bonusHealth;
        killCount+=50;
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
        sfxExplode.stop();
        sfxExplode.cue(0);
        sfxExplode.play();
        health -= enemy_damage*1.3;
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
        sfxExplode.stop();
        sfxExplode.cue(0);
        sfxExplode.play();
      if (health > 0) {
        health -= enemy_damage;
        enemyList.remove(i--);
        skipcheck=true;
        continue;
      }
    }
    for (int j = 0; j < projectileList.size(); j++) {
      Projectile proj = projectileList.get(j);
      if (checkHit(proj, enemy) && !skipcheck) {
        sfxExplode.stop();
        sfxExplode.cue(0);
        sfxExplode.play();
        Explosion newExplosion = new Explosion("explosion",16,64,64,proj.x,proj.y-50);
        explosionList.add(newExplosion);
        enemyList.remove(i--);
        projectileList.remove(j--);
        killCount+=250;
      }
    }
  }



      
   // check hit with enemy projectile
   for (int j = 0; j < enemyProjectileList.size(); j++) {
    EnemyProjectile proj = enemyProjectileList.get(j);
    if (checkHit(proj, monkey)) {
      //enemyList.remove(i--);
      health-=25;
      if(health<0) {
        Explosion newExplosion = new Explosion("explosion",16,64,64,proj.x,proj.y-50);
        explosionList.add(newExplosion);  
        sfxExplode.stop();
        sfxExplode.cue(0);
        sfxExplode.play();
      }
      else {
        sfxExplodeSmall.stop();
        sfxExplodeSmall.cue(0);
        sfxExplodeSmall.play();
        Dusthit newHit = new Dusthit("dust",8,64,64,proj.x-20,proj.y-20);
        dustList.add(newHit);
      }
      enemyProjectileList.remove(j--);
      
      }
    }

    
   // check hit with asteorids
   for (int i = 0; i < asteroidList.size(); i++) {
    Asteroid asteroid = asteroidList.get(i);
    for (int j = 0; j < projectileList.size(); j++) {
      Projectile proj = projectileList.get(j);
      if (checkHit(proj, asteroid)) {
        //enemyList.remove(i--);
        asteroid.health-=240;
        if(asteroid.health<0) {
          asteroidList.remove(i--);
          Explosion newExplosion = new Explosion("explosion",16,64,64,proj.x,proj.y-50);
          explosionList.add(newExplosion);  
          sfxExplode.stop();
          sfxExplode.cue(0);
          sfxExplode.play();
          killCount+=150;
        }
        else {
          sfxExplodeSmall.stop();
          sfxExplodeSmall.cue(0);
          sfxExplodeSmall.play();
          Dusthit newHit = new Dusthit("dust",8,64,64,proj.x-20,proj.y-20);
          dustList.add(newHit);
        }
        projectileList.remove(j--);
        
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
  
  for (int i = 0; i < enemyProjectileList.size(); i++) {
    EnemyProjectile proj = enemyProjectileList.get(i);
    proj.move();
    proj.disp();
    if (proj.y + proj.h < 0)
      enemyProjectileList.remove(i--);
    if (proj.y + proj.h > height)
      enemyProjectileList.remove(i--);
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
  fill(#00FF00);
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
    //background(bgdead);
    image(bgdead);
    
    rectMode(CORNER);
      fill(button_over_color);
      if (mousePressed && mouseY < 100 ) {
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
    textAlign(CENTER);
    //text("... after only "+seconds+" seconds", button_x0, button_y0+18, button_w, button_h);
    ellipse(button_x0+100, startbox_y1-5, button_w, button_h);
    fill(#000000);
    textAlign(CENTER);
    text("Oh no, you died...", button_x0, startbox_y1-23, button_w, button_h);
    text("Final score: "+killCount, button_x0, startbox_y1-9, button_w, button_h);
    text("Click here to try again", button_x0, startbox_y1+7, button_w, button_h);
    
    }

}

void parallax(PImage img, PVector pos, PVector vel, String direction) {
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

void changeSpeed() {
  float acc = .25;
  if (mouseButton == LEFT){
  vel_dust.y -=1;
  vel_stars.y -=2;
  }else {
  vel_dust.y +=1;
  vel_stars.y +=2;
  }
 }


//Checks to see if the two sprites are overlapping
boolean checkHit(Sprites a, Sprites b) {
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

void shuffle(int[] a)
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

void keyPressed() {
  up = up || key == 'w' || key == 'W' || keyCode == UP;
  down = down || key == 's' || key == 'S' || keyCode == DOWN;
  left = left || key == 'a' || key == 'A' || keyCode == LEFT;
  right = right || key == 'd' || key == 'D' || keyCode == RIGHT;
  fire = fire || key == ' ';

}

void keyReleased() {
  up = up && !(key == 'w' || key == 'W' || keyCode == UP);
  down = down && !(key == 's' || key == 'S' || keyCode == DOWN);
  left = left && !(key == 'a' || key == 'A' || keyCode == LEFT);
  right = right && !(key == 'd' || key == 'D' || keyCode == RIGHT);
  fire = fire && !(key == ' ');
}

void getButtonStates() {
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

//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


PImage [] loadImages(String stub, String extension, int numImages)
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
  public color c = #FFFFFF;
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
  private int alientype=0;
  
  Enemy(String imgname){
    spriteimg = loadImages(imgname, ".png", 1);
    if(imgname=="alien1")alientype=1;
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
    if(alientype==1){
    if((seconds%2)==1) y -= v_y;
    else y += v_y*3;
    }
    else {
      y += v_y*3;
    }
    tx=tx+(v_x_timestep/4)%TWO_PI;
    x = (int)(x_0 + v_x_amp*sin(tx));
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


// shoots back now...
class EnemyProjectile extends Sprites {
  private int currentFrame=0;
  private int numFrames=0;

  EnemyProjectile(String imgname, int frames, int imwidth, int imheight){
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
  public int v_x = 0, v_y = 10;
  //Use the members to move the object
  public void move() {
    this.x += this.v_x;
    this.y += v_y;
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
  public int health = 1000;
  
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
    tx = (tx + v_x_timestep/2) % TWO_PI;
    x = (int)(x_0/2 + v_x_amp*sin(tx));
    y += v_y*1.8;
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
    currentFrame=0;
    killed=0;
  }
 
  public void disp(){
   image(spriteimg[(int)currentFrame],x, y,w, h);
   currentFrame++;
   if(currentFrame>=numFrames){currentFrame=15;killed=1;}
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
AudioPlayer sfxExplodeSmall;
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
final color bg = 50;
final int game_w = 320; // # pixels wide in "game-space"
final int game_h = 480; // # pixels tall in "game-space"
float game_s; //games's scale value (used to move between game and
              //screen position values
final color game_padding = #000000; //The color used when the aspect ratio
                                    //of the game differs from the screen
final int fr = 100; //framerate
int killCount = 0; //the score

//Player properties
final int player_w = 51;
final int player_h = 84;
final int player_x = game_w/2;
final int player_y = (8*game_h)/10; 
final int player_x_min = 10;
final int player_x_max = (8*game_w)/10;
final int player_y_min = (2*game_h)/3;
final int player_y_max = (8*game_h)/10;
final int player_v_x = 3, player_v_y = 2;
final color player_c = #00FFFF;

//Healthbar properties
final int health_w_max = game_w - 4;
final int health_h = 3;
final int health_x = 2;
final int health_y = game_h - health_h - 2;
final int health_max = 100;
final color health_c = #0011FF;
int health = 100;

//Enemy properties
final int enemy_w = 64;
final int enemy_h = 64;
final float enemy_v_x_amp = 0.3f*(float)game_w;
final float enemy_v_x_timestep = HALF_PI/fr;
final int enemy_v_y = 1;
final float star_v_y = 1;

final int enemy_damage = 22;
final color enemy_c = #FF0000;
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
int coolDown=500;

//Restart button properties
final int button_w = 200;
final int button_h = 64;
final int button_x0 = game_w/2 - button_w/2;
final int button_x1 = button_x0 + button_w;
final int button_y0 = game_h/2 - button_h/2 - 180;
final int button_y1 = button_y0 + button_h;
final int startbox_y1=button_y0+300;
final color button_idle_color = #00FF00;
final color button_over_color = #FFFFFF;


//Player and other entities
Monkey monkey;
Sprites healthBar;
Enemy enemy;
Asteroid asteroid;
Banana banana;
Stars star;
Explosion explosion;
Dusthit dusthit;
EnemyProjectile enemyProjectile;

ArrayList<Projectile> projectileList;
ArrayList<EnemyProjectile> enemyProjectileList;
ArrayList<Enemy> enemyList;
ArrayList<Asteroid> asteroidList;
ArrayList<Banana> bananaList;
ArrayList<Stars> starsList;
ArrayList<Explosion> explosionList;
ArrayList<Dusthit> dustList;


