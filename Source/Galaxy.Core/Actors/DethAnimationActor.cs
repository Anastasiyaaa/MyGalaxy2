#region using

using System.Diagnostics;
using Galaxy.Core.Environment;

#endregion

namespace Galaxy.Core.Actors
{
    public abstract class DethAnimationActor : BaseActor
    {
        protected DethAnimationActor(ILevelInfo info)
            : base(info)
        {
        }

        #region Constant

        private const long DethTimeMs = 100;

        #endregion

        #region Private fields

        private bool m_dethAnimation;
        private Stopwatch m_dethTimer;
        private bool m_isAlive;

        #endregion

        #region public properties

        public override bool IsAlive
        {
            get { return m_isAlive; }
            set
            {
                m_isAlive = value;
                if (m_isAlive) return;

                m_dethAnimation = !value;

                h_startDethAnimation();
            }
        }

        #endregion

        #region Overrides

        public override void Update()
        {
            base.Update();

            h_checkDeth();
        }

        #endregion

        #region Private methods

        private void h_checkDeth()
        {
            if (m_dethAnimation && m_dethTimer.ElapsedMilliseconds > DethTimeMs)
            {
                CanDrop = true;
            }
        }

        private void h_startDethAnimation()
        {
            m_dethTimer = new Stopwatch();
            m_dethTimer.Start();
            Load(@"Assets\deth.png");
        }

        #endregion
    }
}
