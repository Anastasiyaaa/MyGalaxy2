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
    class Lightning: BaseActor
    {
        #region Private fields

        private bool flag;
        private bool m_isAlive;
        private int direction;
        private int changeX;
        private int changeY;

        #endregion

        #region Constructors

        public Lightning(ILevelInfo info)
            : base(info)
        {
            Width = 30;
            Height = 30;
            ActorType = ActorType.Lightning;
        }

        #endregion

        public override bool IsAlive
        {
            get   { return m_isAlive; }
            set
            {
                m_isAlive = true;

                CanDrop = false;
            }
        }

        #region Overrides

        public override void Update()
        {
            base.Update();
            h_changePosition();
        }

        #endregion

        #region Overrides

        public override void Load()
        {
            Load(@"Assets\Lightning.png");
            firstDirection();
        }

        #endregion

        #region Private methods

        private void firstDirection()
        {
            Random rnd = new Random();
            direction = rnd.Next(1, 5);
            flag = direction > 2;
        }
        private void h_changePosition()
        {
            Size levelSize = Info.GetLevelSize();

            if (direction == 1)
            {
                changeX = -3;
                changeY = -3;
            }

            if (direction == 2)
            {
                changeX = 3;
                changeY = 3;
            }

            if (direction == 3)
            {
                changeX = -3;
                changeY = 3;
            }

            if (direction == 4)
            {
                changeX = 3;
                changeY = -3;
            }


            if (flag)
            {
                if (Position.X > levelSize.Width - 53 || Position.Y > levelSize.Height - 70)
                {
                    changeX = -3;
                    changeY = -3;
                    direction = 1;
                    flag = false;
                }

                if (Position.X < 3 || Position.Y < 3)
                {
                    changeX = 3;
                    changeY = 3;
                    direction = 2;
                    flag = false;
                }
            }
            else
            {
                if (Position.X < 3 || Position.Y > levelSize.Height - 70)
                {
                    changeX = 3;
                    changeY = -3;
                    direction = 4;
                    flag = true;
                }
                if (Position.X > levelSize.Width - 53 || Position.Y < 3)
                {
                    changeX = -3;
                    changeY = 3;
                    direction = 3;
                    flag = true;
                }
            }

            Position = new Point((int)(Position.X + changeX), (int)(Position.Y + changeY));
        }

        #endregion
    }
}
