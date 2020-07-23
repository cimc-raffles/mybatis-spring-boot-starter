package cimc.raffles.mybatis.exception;

public class CustomMessageException extends RuntimeException 
{

	private static final long serialVersionUID = 1L;


	public CustomMessageException() {
		super() ;
	}

	public CustomMessageException(String message) {
		super( message);
	}

	public CustomMessageException(Throwable cause) {
		super( cause);
	}

	
    public CustomMessageException(String message, Throwable cause) {
        super( message, cause);
    }
}
