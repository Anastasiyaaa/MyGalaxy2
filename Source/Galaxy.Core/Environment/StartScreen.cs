#region using

using System;

#endregion

namespace Galaxy.Core.Environment
{
    public class StartScreen : BaseLevel
    {
        public StartScreen()
        {
            FileName = @"Assets\StartScreen.png";
        }
        #region public properties

        public static Type LevelOne { get; set; }

        #endregion

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
            return (BaseLevel)Activator.CreateInstance(LevelOne);
        }

    }
}
