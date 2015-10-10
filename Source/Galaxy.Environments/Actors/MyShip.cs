using System;
using System.Diagnostics;
using System.Windows;
using Galaxy.Core.Actors;
using Galaxy.Core.Environment;
using Point = System.Drawing.Point;
using Size = System.Drawing.Size;


namespace Galaxy.Environments.Actors
{
    class MyShip: Ship
    {
    
        public MyShip(ILevelInfo info)
            : base(info)
        {
            Width = 40;
            Height = 35;
            ActorType = ActorType.Enemy;
        }

        public override void Load()
        {
            base.Load();
            Load(@"Assets\myship.png");
            
        }

        public override void Update()
        {
            InitTimer();
            EnemyBullet enbBullet = new EnemyBullet(Info);
        }
        

    }
}
