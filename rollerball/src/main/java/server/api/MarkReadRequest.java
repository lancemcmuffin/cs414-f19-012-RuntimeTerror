package server.api;

public class MarkReadRequest extends AuthenticatedRequest{
    public int id;
    public boolean delete;
}