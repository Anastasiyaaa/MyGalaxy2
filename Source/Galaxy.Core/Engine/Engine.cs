#region using

using System;
using System.Diagnostics;
using System.Drawing;
using System.Threading;
using System.Windows.Forms;
using Galaxy.Core.Environment;

#endregion

namespace Galaxy.Core.Engine
{
    public class Engine
    {
        #region Private fields

        private readonly PictureBox m_canvas;
        private BaseLevel m_level;

        #endregion

        #region Constructors

        public Engine(PictureBox canvas, Type firstLevel)
        {
            m_canvas = canvas;
            m_targetFps = 60;

            StartScreen.LevelOne = firstLevel;
        }

        #endregion

        #region Public methods

        public void Start()
        {
            if (IsRunning)
            {
                return;
            }

            Level = new StartScreen();

            IsRunning = true;
            h_runningThread = new Thread(h_threadRun);
            h_runningThread.Start();
        }

        public void Stop()
        {
            IsRunning = false;
        }

        #endregion

        #region Private methods

        private void h_threadRun()
        {
            var framecounter = new Stopwatch();
            var tickcounter = new Stopwatch();

            while (IsRunning)
            {
                framecounter.Start();
                tickcounter.Start();

                h_tick();

                tickcounter.Stop();

                var targettime = 1f / m_targetFps * 1000f;
                var actualtime = tickcounter.ElapsedMilliseconds;
                var sleep = targettime - actualtime;
                if (sleep > 0)
                {
                    Thread.Sleep((int)sleep);
                }

                tickcounter.Reset();
                framecounter.Stop();

                if (framecounter.ElapsedMilliseconds < 1000)
                {
                    continue;
                }

                framecounter.Reset();
            }
        }

        private void h_tick()
        {
            var frame = new Bitmap(Level.Size.Width, Level.Size.Height);

            using (var graphics = Graphics.FromImage(frame))
            {
                Level.Update();
                Level.Draw(graphics);
                graphics.Flush();
            }

            if (Level.Success)
                Level = Level.NextLevel();

            if (Level.Failed)
            {
                Level = new FailScreen();
            }

            if (m_canvas.Image != null)
            {
                m_canvas.Image.Dispose();
            }
            m_canvas.Image = frame;
        }

        #endregion

        #region Props

        private readonly int m_targetFps;

        public bool IsRunning { get; private set; }

        private Thread h_runningThread { get; set; }

        public BaseLevel Level
        {
            get { return m_level; }
            set
            {
                m_level = value;
                m_level.Load();
                m_canvas.Width = m_level.Size.Width;
                m_canvas.Height = m_level.Size.Height;
            }
        }

        #endregion
    }
}
