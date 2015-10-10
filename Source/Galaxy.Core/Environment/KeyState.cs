using System;
using System.Runtime.InteropServices;

namespace Galaxy.Core.Environment
{
    public class KeyState
    {
        public static bool IsPressed(VirtualKeyStates key)
        {
            return Convert.ToBoolean(GetKeyState(key) & 0x8000);
        }

        [DllImport("USER32.dll")]
        private static extern short GetKeyState(VirtualKeyStates nVirtKey);
    }
}