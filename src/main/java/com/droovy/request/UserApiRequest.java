package com.droovy.request;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse.Status;

import errors.ApplicationException;
import errors.UserFaultException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

@Path("request")
public class UserApiRequest {
	
	
	UserRequest request_dropbox = new UserRequestDropbox();
	UserRequest request_googledrive = new UserRequestGoogleDrive();
	UserRequest request_onedrive = new UserRequestOneDrive();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public Response getFilesList(@Context UriInfo uriInfo,@QueryParam("path") String path,@QueryParam("idUser") String idUser,@QueryParam("idFolder") String idFolder,@QueryParam("getDropbox") int getDropbox,@QueryParam("getGoogleDrive") int getGoogledrive,@QueryParam("getOnedrive") int getOnedrive) throws JsonProcessingException, ApplicationException, UserFaultException {
				
		List<File> listDropbox = new LinkedList<>(), listGoogleDrive = new LinkedList<>(),listOneDrive = new LinkedList<>();
		
		if(getDropbox==1) {			
			listDropbox = request_dropbox.getFilesList(path,idUser);
		}
		if(getGoogledrive==1) {
			listGoogleDrive = request_googledrive.getFilesList(idFolder,idUser);
		}
		if(getOnedrive==1) {
			listOneDrive = request_onedrive.getFilesList(path, idUser);
		}
		
		Merger merge = new Merger();
		
		List<File> mergedList = merge.merge(listGoogleDrive, listDropbox, listOneDrive);
		
		ObjectMapper mapper = new ObjectMapper();
		
		String output = "[";

		for (File file : mergedList) {
			
			output = output + mapper.writeValueAsString(file)+",";
		}

		if(mergedList.isEmpty()) {
			output += "]";
		}
		else {
			output = output.substring(0,output.length()-1);//Retire la virgule en trop
			output += "]";
		}
		
		return Response.status(Status.OK).entity(output).build();
	}
	

	
	@POST
	@Produces("text/plain")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("/upload")
	public String uploadFile(@FormDataParam("file") InputStream uploadInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, @QueryParam("idUser") int idUser, @QueryParam("drive") String drive) throws IOException {
	
		OutputStream outputStream = new FileOutputStream(new java.io.File(fileDetail.getFileName()));
	
		
		/*Sockage du fichier en local*/
		int read = 0;
		byte[] bytes = new byte[150000000];
	
		while ((read = uploadInputStream.read(bytes)) != -1) {
			outputStream.write(bytes, 0, read);
		}

		outputStream.close();
		uploadInputStream.close();
		
		
		request_dropbox.uploadFile(fileDetail.getFileName(), "/test/"+fileDetail.getFileName(), ""+2);
		
		return fileDetail.getFileName()+" "+fileDetail.getSize()+" "+fileDetail.getType()+" "+fileDetail;
	}

	@GET
	@Produces("text/plain")
	@Path("/delete")
	public String deleteFile( @QueryParam("idUser") String idUser, @QueryParam("path") String path, @QueryParam("idFile") String idFile,@QueryParam("drive") String drive) throws IOException {
		
		if(drive.equals("dropbox")) {
			request_dropbox.removeFile(idFile, path, idUser);
		}
		else if(drive.equals("onedrive")) {
			request_onedrive.removeFile(idFile, path, idUser);
		}
		else if(drive.equals("googledrive")) {
			request_googledrive.removeFile(idFile, path, idUser);
		}
		return "";
	}
	
	@GET
	@Produces("text/plain")
	@Path("/rename")
	public String renameFile( @QueryParam("idUser") String idUser, @QueryParam("path") String path, @QueryParam("idFile") String idFile,@QueryParam("drive") String drive, @QueryParam("name") String name) throws IOException {

		if(drive.equals("dropbox")) {
			request_dropbox.renameFile(idFile, path, name, idUser);
		}
		else if(drive.equals("onedrive")) {
			request_onedrive.renameFile(idFile, path, name, idUser);
		}
		else if(drive.equals("googledrive")) {
			request_googledrive.renameFile(idFile, path, name, idUser);
		}
		return "";
	}
	
	@GET
	@Produces("text/plain")
	@Path("/move")
	public String moveFile( @QueryParam("idUser") String idUser, @QueryParam("path") String path, @QueryParam("idFile") String idFile,@QueryParam("drive") String drive, @QueryParam("idParent") String idParent, @QueryParam("pathParent") String pathParent) throws IOException {

		if(drive.equals("dropbox")) {
			request_dropbox.moveFile(idFile, path, idParent, pathParent, idUser);
		}
		else if(drive.equals("onedrive")) {
			request_onedrive.moveFile(idFile, path, idParent, pathParent, idUser);
		}
		else if(drive.equals("googledrive")) {
			request_googledrive.moveFile(idFile, path, idParent, pathParent, idUser);
		}
		return "";
	}
	@GET
	@Produces("text/plain")
	@Path("/freespace")
	public String freeSpace( @QueryParam("idUser") String idUser, @QueryParam("drive") String drive) throws IOException {
		
		if(drive.equals("dropbox")) {
			return request_dropbox.freeSpaceRemaining(idUser);
		}
		else if(drive.equals("onedrive")) {
			return request_onedrive.freeSpaceRemaining(idUser);
		}
		else if(drive.equals("googledrive")) {
			return request_googledrive.freeSpaceRemaining(idUser);
		}
		return "{}";
			
	}
	
}
