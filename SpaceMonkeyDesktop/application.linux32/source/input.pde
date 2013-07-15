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

