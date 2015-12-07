using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Galaxy.Core.Actors;
using Galaxy.Core.Environment;

namespace Galaxy.Environments.Actors
{
    public class EnemyBullet : BaseActor
    {
        #region Constant

        private const int Speed = 5;

        #endregion

        private bool m_isAlive;

        #region Constructors

        public EnemyBullet(ILevelInfo info)
            : base(info)
        {
            Width = 3;
            Height = 3;
            ActorType = ActorType.Enemy;
        }

        #endregion

        #region Overrides

        public override bool IsAlive
        {
            get { return m_isAlive; }
            set
            {
                m_isAlive = !value;
                CanDrop = value;
            }
        }
        public override void Load()
        {
            Load(@"Assets\bullet.png");
        }

        public override void Update()
        {
            Size levelSize = Info.GetLevelSize();

            Position = new Point(Position.X, Position.Y + Speed);
            IsAlive = Position.Y > levelSize.Height;
        }

        #endregion
    }
}

