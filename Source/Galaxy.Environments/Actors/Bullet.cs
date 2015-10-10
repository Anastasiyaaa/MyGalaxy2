#region using

using System.Drawing;
using Galaxy.Core.Actors;
using Galaxy.Core.Environment;

#endregion

namespace Galaxy.Environments.Actors
{
    public class Bullet : BaseActor
    {
        #region Constant

        private const int Speed = 10;

        #endregion

        #region Constructors

        public Bullet(ILevelInfo info)
            : base(info)
        {
            Width = 3;
            Height = 3;
            ActorType = ActorType.Player;
        }

        #endregion

        #region Overrides

        public override void Load()
        {
            Load(@"Assets\bullet.png");
        }

        public override void Update()
        {
            Position = new Point(Position.X, Position.Y - Speed);
        }

        #endregion
    }
}
