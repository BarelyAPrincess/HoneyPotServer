package com.marchnetworks.web.test;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet( {"/DeviceCapabilityTestServlet"} )
public class DeviceCapabilityTestServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/* Error */
	protected void doGet( javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response ) throws javax.servlet.ServletException, java.io.IOException
	{
		throw new IllegalStateException( "Not Decompiled!" );

		// Byte code:
		//   0: aload_2
		//   1: invokeinterface 2 1 0
		//   6: astore_3
		//   7: aload_3
		//   8: invokevirtual 3	javax/servlet/ServletOutputStream:close	()V
		//   11: goto +12 -> 23
		//   14: astore 4
		//   16: aload_3
		//   17: invokevirtual 3	javax/servlet/ServletOutputStream:close	()V
		//   20: aload 4
		//   22: athrow
		//   23: return
		// Line number table:
		//   Java source line #33	-> byte code offset #0
		//   Java source line #43	-> byte code offset #7
		//   Java source line #44	-> byte code offset #11
		//   Java source line #43	-> byte code offset #14
		//   Java source line #45	-> byte code offset #23
		// Local variable table:
		//   start	length	slot	name	signature
		//   0	24	0	this	DeviceCapabilityTestServlet
		//   0	24	1	request	javax.servlet.http.HttpServletRequest
		//   0	24	2	response	javax.servlet.http.HttpServletResponse
		//   6	11	3	os	javax.servlet.ServletOutputStream
		//   14	7	4	localObject	Object
		// Exception table:
		//   from	to	target	type
		//   14	16	14	finally
	}
}
