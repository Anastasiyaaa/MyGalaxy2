#region using

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Diagnostics.Eventing.Reader;
using System.Drawing;
using System.Linq;
using Galaxy.Core.Actors;
using Galaxy.Core.Collision;
using Galaxy.Core.Environment;
using Galaxy.Environments.Actors;

#endregion

namespace Galaxy.Environments
{
  /// <summary>
  ///   The level class for Open Mario.  This will be the first level that the player interacts with.
  /// </summary>
      public class LevelOne : BaseLevel
      {
        private int m_frameCount;
        private Stopwatch m_flyTimer;

        #region Constructors

        /// <summary>
        ///   Initializes a new instance of the <see cref="LevelOne" /> class.
        /// </summary>
        public LevelOne()
        {
              // Backgrounds
              FileName = @"Assets\LevelOne.png";

              // Enemies
              for (int i = 0; i < 5; i++)
              {
                var ship = new Ship(this);
                int positionY = ship.Height + 10;
                int positionX = 150 + i * (ship.Width + 50);
              
                ship.Position = new Point(positionX, positionY);

                Actors.Add(ship);
              }
                InitTimer();
        
              //myship
              for (int i = 0; i < 4; i++)
              {
                  var myship = new MyShip(this);
                  int positionY = myship.Height + 50;
                  int positionX = 150 + i * (myship.Width + 50);

                  myship.Direction = i < 2;

                  myship.Position = new Point(positionX, positionY);

                  Actors.Add(myship);
              }

              //Lightning
              Lightning = new Lightning(this);
              int lightningPositionX = Size.Width / 2 - Lightning.Width / 2;
              int lightningPositionY = Size.Height/2 - Lightning.Height/2;

              Lightning.Position = new Point(lightningPositionX, lightningPositionY);
              Actors.Add(Lightning);

              // Player
              Player = new PlayerShip(this);
              int playerPositionX = Size.Width / 2 - Player.Width / 2;
              int playerPositionY = Size.Height - Player.Height - 50;

              Player.Position = new Point(playerPositionX, playerPositionY);
              Actors.Add(Player);
        }

        #endregion

        #region Overrides

        private void h_dispatchKey()
        {
          if (!IsPressed(VirtualKeyStates.Space)) return;

          if(m_frameCount % 10 != 0) return;

            Bullet bullet = new Bullet(this)
            {
                Position = Player.Position
            };

          bullet.Load();
          Actors.Add(bullet);
        }

        public override BaseLevel NextLevel()
        {
          return new StartScreen();
        }

        private void GenerateBullet()
        {
            MyShip[] arrayShip = Actors.Where(actor => actor is MyShip).Cast<MyShip>().ToArray();
            
            if (arrayShip.Count() > 0)
            {
                Random rnd = new Random();
                int num = rnd.Next(0, arrayShip.Count());

                Actors.Add(arrayShip[num].CreatEnemyBullet());  
            }
            
        }

      private void RemoveBullet()
      {
          EnemyBullet[] arrayEnemyBullets = Actors.Where(actor => actor is EnemyBullet).Cast<EnemyBullet>().ToArray();

          foreach (var enemyBullet in arrayEnemyBullets)
          {
              if (enemyBullet.Position.Y > 480 || enemyBullet.Position.Y < 0)
              {
                  Actors.Remove(enemyBullet);
              }
          }
      }
        protected void InitTimer()
        {
            if (m_flyTimer == null)
            {
                m_flyTimer = new Stopwatch();
                m_flyTimer.Start();
            }
        }

        public override void Update()
        {
          m_frameCount++;
          h_dispatchKey();

          base.Update();

          if (m_flyTimer.ElapsedMilliseconds >= 500)
          {
              GenerateBullet();
              m_flyTimer.Restart();
          }

            RemoveBullet();
        
          IEnumerable<BaseActor> killedActors = CollisionChecher.GetAllCollisions(Actors);

          foreach (BaseActor killedActor in killedActors)
          {
              if (killedActor.IsAlive && killedActor.ActorType != ActorType.Lightning)
              killedActor.IsAlive = false;
          }

          List<BaseActor> toRemove = Actors.Where(actor => actor.CanDrop).ToList();
          BaseActor[] actors = new BaseActor[toRemove.Count()];
          toRemove.CopyTo(actors);

          foreach (BaseActor actor in actors.Where(actor => actor.CanDrop))
          {
            Actors.Remove(actor);
          }

          if (Player.CanDrop)
            Failed = true;

          //has no enemy
          if (Actors.All(actor => actor.ActorType != ActorType.Enemy))
            Success = true;
        }

        #endregion
      }
}
