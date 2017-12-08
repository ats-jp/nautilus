package nautilus.test;

import java.io.FileOutputStream;

import java.util.Date;

import java.util.Enumeration;

import com.ibm.as400.access.AS400;

import com.ibm.as400.access.PrintObject;

import com.ibm.as400.access.PrintObjectTransformedInputStream;

import com.ibm.as400.access.PrintParameterList;

import com.ibm.as400.access.SpooledFile;

import com.ibm.as400.access.list.SpooledFileListItem;

import com.ibm.as400.access.list.SpooledFileOpenList;

public

class SpooledFileToPDFExample {

	private static String location = "C:/workspace/POC/JavaPOCCode/result/";

	public static void main(String[] args) {
		FileOutputStream fileoutputstream = null;

		try {
			// AS400 as400Con = new AS400("TESTING.AGF.CA", 
			// "SQAACCJZZ","SQAACCJZZ");
			AS400 as400Con = new AS400("NEWREL", "RAVIH", "RAVIH");
			as400Con.setGuiAvailable(false);
			System.out.println(
				new Date(as400Con.getPreviousSignonDate().getTimeInMillis()));
			System.out.println("getVersion :: " + as400Con.getVersion());
			System.out.println("getVersion :: " + as400Con.getVersion());
			SpooledFileOpenList spooledFileOpenList = new SpooledFileOpenList(
				as400Con);
			// spooledFileOpenList.setFilterJobInformation("JOBACCRORP", 
			// "SQAACCS1RH", "584373"); 
			spooledFileOpenList.setFilterUsers(new String[] { "RAVIH" });
			spooledFileOpenList
				.addSortField(SpooledFileOpenList.JOB_NUMBER, true);
			spooledFileOpenList.open();
			Enumeration<?> spooledFiles = spooledFileOpenList.getItems();
			// The Code is currently written to read all spool file for a user. 
			// we can read one spool file 
			// at a time by just creating a spoolfile using name , number , job 
			// name , job user and job number   

			while (spooledFiles.hasMoreElements()) {
				SpooledFileListItem spooledFileListItem = (SpooledFileListItem) spooledFiles
					.nextElement();
				System.out.println(spooledFileListItem.getName());
				System.out.println(spooledFileListItem.getNumber());
				System.out.println(spooledFileListItem.getJobName());
				System.out.println(spooledFileListItem.getJobUser());
				System.out.println(spooledFileListItem.getJobNumber());
				System.out.println("----------------");
				String fileName = location
					+ spooledFileListItem.getName()
					+ "_"
					+ spooledFileListItem.getJobName()
					+ "_"
					+ spooledFileListItem.getJobNumber()
					+ ".pdf";
				SpooledFile spooledFile =

					new SpooledFile(
						as400Con,
						// AS400
						spooledFileListItem.getName(),
						// splf name
						spooledFileListItem.getNumber(),
						// splf number
						spooledFileListItem.getJobName(),
						// job name
						spooledFileListItem.getJobUser(),
						// job user
						spooledFileListItem.getJobNumber());
				// job number
				PrintParameterList plist = new PrintParameterList();
				plist.setParameter(
					PrintObject.ATTR_WORKSTATION_CUST_OBJECT,
					"/QSYS.LIB/QCTXPDF.WSCST");
				plist.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");
				PrintObjectTransformedInputStream pdfInStream = spooledFile
					.getTransformedInputStream(plist);

				if (pdfInStream != null) {
					byte[] buffer = new byte[64 * 1024];
					fileoutputstream = new FileOutputStream(fileName);
					int bytesRead = 0;
					while ((bytesRead = pdfInStream.read(buffer)) != -1) {
						fileoutputstream.write(buffer, 0, bytesRead);
					}
					fileoutputstream.close();
					pdfInStream.close();
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		System.out.println();
	}
}
