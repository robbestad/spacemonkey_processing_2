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
final int game_h = 420; // # pixels tall in "game-space"
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
final int player_y_max = (8*game_h)/11;
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
final int maxhealth = 150;

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

