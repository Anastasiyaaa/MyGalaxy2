#region using

using System;
using System.Diagnostics;
using System.Windows;
using Galaxy.Core.Actors;
using Galaxy.Core.Environment;
using Point = System.Drawing.Point;
using Size = System.Drawing.Size;

#endregion

namespace Galaxy.Environments.Actors
{
    public class Ship : DethAnimationActor
    {
        #region Constant
        
        private const int MaxSpeed = 3;
        private const long StartFlyMs = 2000;

        #endregion

        #region Private fields

        private bool m_flying;
        private Stopwatch m_flyTimer;

        #endregion

        #region Constructors

        public Ship(ILevelInfo info)
            : base(info)
        {
            Width = 30;
            Height = 30;
            ActorType = ActorType.Enemy;
        }

        #endregion


        #region Overrides

        public override void Update()
        {
            base.Update();

            if (!IsAlive)
                return;

            if (!m_flying)
            {
                if (m_flyTimer.ElapsedMilliseconds <= StartFlyMs) return;

                m_flyTimer.Stop();
                m_flyTimer = null;
                h_changePosition();
                
                m_flying = true;
            }
            else
            {
                h_changePosition();
            }
        }

        #endregion

        #region Overrides

        public override void Load()
        {
            Load(@"Assets\ship.png");
            InitTimer();
        }

        protected void InitTimer()
        {
            if (m_flyTimer == null)
            {
                m_flyTimer = new Stopwatch();
                m_flyTimer.Start();
            }
        }


        #endregion

        #region Private methods


        private void h_changePosition()
        {
            Point playerPosition = Info.GetPlayerPosition();

            Vector distance = new Vector(playerPosition.X - Position.X, playerPosition.Y - Position.Y);
            double coef = distance.X / MaxSpeed;

            Vector movement = Vector.Divide(distance, coef);

            Size levelSize = Info.GetLevelSize();

            if (movement.X > levelSize.Width)
                movement = new Vector(levelSize.Width, movement.Y);

            if (movement.X < 0 || double.IsNaN(movement.X))
                movement = new Vector(0, movement.Y);

            if (movement.Y > levelSize.Height)
                movement = new Vector(movement.X, levelSize.Height);

            if (movement.Y < 0 || double.IsNaN(movement.Y))
                movement = new Vector(movement.X, 0);

            Position = new Point((int)(Position.X + movement.X), (int)(Position.Y + movement.Y));
        }

        #endregion
    }
}
