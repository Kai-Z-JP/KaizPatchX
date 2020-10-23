package jp.ngt.rtm.network;

/**
 * 接続状態の保持
 */
public final class ConnectionManager {
	public static final ConnectionManager INSTANCE = new ConnectionManager();

	private boolean serverConnection;

	private ConnectionManager() {
	}

	public boolean isConnectedToServer() {
		return this.serverConnection;
	}

	/**
	 * Serverと接続(ClientSide)
	 */
	public void onConnectedToServer(boolean isLocal) {
		this.serverConnection = true;
	}

	/**
	 * Clientと接続(ServerSide)
	 */
	public void onConnectedFromClient(boolean isLocal) {
	}
}