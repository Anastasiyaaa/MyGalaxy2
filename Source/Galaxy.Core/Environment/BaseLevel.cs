#region using

using System.Collections.Generic;
using System.Drawing;
using Galaxy.Core.Actors;

#endregion

namespace Galaxy.Core.Environment
{
    public abstract class BaseLevel : ILevelInfo
    {

        #region Constant

        public const int DefaultHeight = 480;
        public const int DefaultWidth = 640;

        #endregion

        #region Private fields

        private Image m_image;
        public Size Size { get; set; }

        #endregion

        #region public properties

        public bool Success { get; set; }
        public bool Failed { get; set; }

        #endregion

        #region Protected properties

        protected List<BaseActor> Actors { get; set; }
        protected string FileName { get; set; }

        #endregion

        #region Constructors

        protected BaseLevel()
        {
            Actors = new List<BaseActor>();

            Size = new Size(DefaultWidth, DefaultHeight);
        }

        #endregion

        #region Public methods

        public void Draw(Graphics graphics)
        {
            h_draw(graphics);

            foreach (BaseActor actor in Actors)
            {
                actor.Draw(graphics);
            }
        }

        public void Load()
        {
            h_load();

            foreach (BaseActor actor in Actors)
            {
                actor.Load();
            }
        }

        public abstract BaseLevel NextLevel();

        public virtual void Update()
        {
            BaseActor[] _actors = new BaseActor[Actors.Count];
            Actors.CopyTo(_actors);
            foreach (BaseActor baseActor in _actors)
            {
                baseActor.Update();
            }
        }

        protected bool IsPressed(VirtualKeyStates key)
        {
            return KeyState.IsPressed(key);
        }

        #endregion

        #region Private methods

        private void h_draw(Graphics graphics)
        {
            graphics.DrawImage(m_image, 0, 0);
        }

        private void h_load()
        {
            m_image = (Bitmap)Image.FromFile(FileName);
            m_image = new Bitmap(m_image, Size);
        }

        #endregion

        #region Implementation of ILevelInfo

        public Point GetPlayerPosition()
        {
            return Player.Position;
        }

        public Size GetLevelSize()
        {
            return Size;
        }


        protected BaseActor Player { get; set; }

        #endregion
    }
}