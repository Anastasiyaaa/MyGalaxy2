#region using

using System.Drawing;
using Galaxy.Core.Actors;
using Galaxy.Core.Environment;

#endregion

namespace Galaxy.Environments.Actors
{
    public class PlayerShip : DethAnimationActor
    {
        #region Constant

        private const int Speed = 3;

        #endregion

        #region Constructors

        public PlayerShip(ILevelInfo info)
            : base(info)
        {
            Width = 22;
            Height = 26;
            ActorType = ActorType.Player;
        }

        #endregion

        #region Overrides

        public override void Load()
        {
            Load(@"Assets\player.png");
        }

        #region Overrides of DethAnimationActor

        public override void Update()
        {
            base.Update();

            if (IsPressed(VirtualKeyStates.Left))
                Position = new Point(Position.X - Speed, Position.Y);
            if (IsPressed(VirtualKeyStates.Right))
                Position = new Point(Position.X + Speed, Position.Y);
        }

        #endregion

        #endregion
    }
}
