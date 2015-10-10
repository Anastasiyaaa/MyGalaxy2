#region using

#endregion

namespace Galaxy.Core.Environment
{
    public class FailScreen : BaseLevel
    {
        public FailScreen()
        {
            FileName = @"Assets\FailScreen.png";
        }
        #region Overrides

        #region Overrides of BaseLevel

        public override void Update()
        {
            base.Update();

            if (IsPressed(VirtualKeyStates.Return))
                Success = true;
        }

        #endregion

        public override BaseLevel NextLevel()
        {
            return new StartScreen();
        }

        #endregion
    }
}
