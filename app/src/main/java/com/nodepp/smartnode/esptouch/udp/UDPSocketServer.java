package com.nodepp.smartnode.esptouch.udp;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import nodepp.Nodepp;

public class UDPSocketServer {

	private static final String TAG = "UDPSocketServer";
	private DatagramPacket mReceivePacket;
	private DatagramSocket mServerSocket;
	private Context mContext;
	private WifiManager.MulticastLock mLock;
	private final byte[] buffer;
	private volatile boolean mIsClosed;



	private synchronized void acquireLock() {
		if (mLock != null && !mLock.isHeld()) {
			mLock.acquire();
		}
	}

	private synchronized void releaseLock() {
		if (mLock != null && mLock.isHeld()) {
			try {
				mLock.release();
			} catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
		}
	}

	/**
	 * Constructor of UDP Socket Server
	 * 
	 * @param port
	 *            the Socket Server port
	 * @param socketTimeout
	 *            the socket read timeout
	 * @param context
	 *            the context of the Application
	 */
	public UDPSocketServer(int port, int socketTimeout, Context context) {
		this.mContext = context;
		this.buffer = new byte[64];
		this.mReceivePacket = new DatagramPacket(buffer, 64);
		try {
			this.mServerSocket = new DatagramSocket(port);
			this.mServerSocket.setSoTimeout(socketTimeout);
			this.mIsClosed = false;
			WifiManager manager = (WifiManager) mContext
					.getSystemService(Context.WIFI_SERVICE);
			mLock = manager.createMulticastLock("test wifi");
			Log.d(TAG, "mServerSocket is created, socket read timeout: "
					+ socketTimeout + ", port: " + port);
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			e.printStackTrace();
		}
	}
	public Nodepp.Msg receiveData(){
		if (mReceivePacket != null && mServerSocket != null){
			setSoTimeout(60000);
			while (!mServerSocket.isClosed()){
				try {
					mServerSocket.receive(mReceivePacket);
					byte[] recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
					if (recDatas != null){
						Nodepp.Msg msg = PbDataUtils.parserResponse(recDatas, recDatas.length);
						if (msg != null){
							Constant.ip= mReceivePacket.getAddress();
							Constant.tempPort = mReceivePacket.getPort();
							Log.i(TAG, "receive client msg :"+msg.toString());
							Log.i(TAG, "ip :"+Constant.ip.getHostName());
							Log.i(TAG, "port :"+Constant.tempPort);
							mServerSocket.close();
							return msg;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return null;
	}
	/**
	 * Set the socket timeout in milliseconds
	 *
	 * @param timeout
	 *            the timeout in milliseconds or 0 for no timeout.
	 * @return true whether the timeout is set suc
	 */
	public boolean setSoTimeout(int timeout) {
		try {
			if (mServerSocket != null){
				this.mServerSocket.setSoTimeout(timeout);
				return true;
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Receive one byte from the port and convert it into String
	 * 
	 * @return
	 */
	public byte receiveOneByte() {
		Log.d(TAG, "receiveOneByte() entrance");
		try {
			acquireLock();
			mServerSocket.receive(mReceivePacket);
			Log.d(TAG, "receive: " + (0 + mReceivePacket.getData()[0]));
			return mReceivePacket.getData()[0];
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Byte.MIN_VALUE;
	}
	
	/**
	 * Receive specific length bytes from the port and convert it into String
	 * 21,24,-2,52,-102,-93,-60
	 * 15,18,fe,34,9a,a3,c4
	 * @return
	 */
	public byte[] receiveSpecLenBytes(int len) {
		Log.d(TAG, "receiveSpecLenBytes() entrance: len = " + len);
		try {
			acquireLock();
			mServerSocket.receive(mReceivePacket);
			if (mReceivePacket != null){
				InetAddress address = mReceivePacket.getAddress();
				int port = mReceivePacket.getPort();
				SocketAddress socketAddress = mReceivePacket.getSocketAddress();
				Log.d(TAG, "address: " + address);
				Log.d(TAG, "getPort: " +port);
				Constant.ip = address;
				Constant.tempPort = port;
				Log.d(TAG, "socketAddress: " + socketAddress);
			}
			byte[] recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
			if (recDatas.length != len) {
				Log.w(TAG,
						"received len is different from specific len, return null");
				return null;
			}
			return recDatas;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void interrupt() {
		Log.i(TAG, "USPSocketServer is interrupt");
		close();
	}

	public synchronized void close() {
		if (!this.mIsClosed) {
			Log.e(TAG, "mServerSocket is closed");
			mServerSocket.close();
			releaseLock();
			this.mIsClosed = true;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

}
