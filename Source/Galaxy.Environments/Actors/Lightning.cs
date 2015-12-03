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
    class Lightning: DethAnimationActor
    {
        #region Private fields

        private bool flag;
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
            FirstDirection();
        }

        #endregion

        #region Private methods

        private void FirstDirection()
        {
            Random rnd=new Random();
            direction = rnd.Next(1, 4);
            flag = direction > 2;
        }
        private void h_changePosition()
        {
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
                if (Position.X > 587 || Position.Y > 410)
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
                if (Position.X < 3 || Position.Y > 410)
                {
                    changeX = 3;
                    changeY = -3;
                    direction = 4;
                    flag = true;
                }
                if (Position.X > 587 || Position.Y < 3)
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
