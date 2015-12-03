using System;
using System.Diagnostics;
using System.Security.Authentication.ExtendedProtection;
using System.Windows;
using Galaxy.Core.Actors;
using Galaxy.Core.Environment;
using Point = System.Drawing.Point;
using Size = System.Drawing.Size;


namespace Galaxy.Environments.Actors
{
    class MyShip : DethAnimationActor
    {
        #region Constant

        private const long StartFlyMs = 2000;

        #endregion

        #region Private fields

        private bool pol;
        private bool m_flying;
        private Stopwatch m_flyTimer;

        #endregion

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

        public MyShip(ILevelInfo info)
            : base(info)
        {
            Width = 40;
            Height = 35;
            ActorType = ActorType.Enemy;
        }

        public bool Direction
        {
            get { return pol; }
            set { pol = value; }
        }
        protected void InitTimer()
        {
            if (m_flyTimer == null)
            {
                m_flyTimer = new Stopwatch();
                m_flyTimer.Start();
            }
        }
        public override void Load()
        {
            Load(@"Assets\myship.png");
            InitTimer();
        }
        public EnemyBullet CreatEnemyBullet()
        {
            var enemybullet = new EnemyBullet(Info);
            int positionY = Position.Y + 20;
            int positionX = Position.X + 20;

            enemybullet.Position = new Point(positionX, positionY);
            enemybullet.Load();

            return enemybullet;
        }

        private void h_changePosition()
        {
            int i;
            if (Direction == false)
            {
                i = 2;
            }
            else
            {
                i = -2;
            }
            Position = new Point((Position.X + i), (Position.Y + 2));
        }
    }
}
