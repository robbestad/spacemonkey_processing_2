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
