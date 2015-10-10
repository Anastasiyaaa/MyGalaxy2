#region using

using System;
using System.Diagnostics;
using System.Drawing;
using Galaxy.Core.Environment;

#endregion

namespace Galaxy.Core.Actors
{
    public abstract class BaseActor
    {
        private bool m_isAlive;
        protected ILevelInfo Info { get; set; }

        #region public properties

        public int Width { get; protected set; }
        public int Height { get; protected set; }
        public Point Position { get; set; }
        public ActorType ActorType { get; protected set; }

        public virtual bool IsAlive
        {
            get { return m_isAlive; }
            set
            {
                m_isAlive = value;
                CanDrop = value;
            }
        }

        public bool CanDrop { get; protected set; }

        #endregion

        #region Protected properties

        protected Bitmap Image { get; set; }

        #endregion

        #region Constructors

        protected BaseActor(ILevelInfo info)
        {
            Info = info;
            Position = new Point(0, 0);
            IsAlive = true;
            CanDrop = false;
        }

        #endregion

        #region Public methods

        public virtual void Draw(Graphics g)
        {
            g.DrawImage(Image, Position.X, Position.Y);
        }

        public abstract void Load();

        public bool IsPressed(VirtualKeyStates key)
        {
            var isPressed = KeyState.IsPressed(key);
            //
            return isPressed;
        }

        public virtual void Update()
        {
            Size levelSize = Info.GetLevelSize();

            if (Position.X > levelSize.Width)
                Position = new Point(0, Position.Y);

            if (Position.X < 0)
                Position = new Point(levelSize.Width, Position.Y);

            if (Position.Y > levelSize.Height)
                Position = new Point(Position.X, 0);

            if (Position.Y < 0)
                Position = new Point(Position.X, levelSize.Height);
        }

        #endregion

        #region Protected methods

        protected void Load(string fileName)
        {
            Image = (Bitmap)System.Drawing.Image.FromFile(fileName);
            Image = new Bitmap(Image, new Size(Width, Height));
        }

        #endregion
    }
}
