/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.predelivery.dao;

import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.predelivery.litmus.helper.CompleteLitmusItem;
import com.agnitas.predelivery.litmus.helper.LitmusPollingItem;

/**
 * DAO interface for predelivery
 */
public interface ComPredeliveryDao {
	
	public final static int PREDELIVERY_TEST_NON_EXISTING = -1;
	
	/** Indicates a new Litmus test waiting to be processed. */
	public final static int PREDELIVERY_TEST_STARTED = 0;
	
	/** Indicates a completed Litmus test. */
	public final static int PREDELIVERY_TEST_COMPLETED = 1;
	
	/** Indicates a Litmus test currently under processing. */
	public final static int PREDELIVERY_TEST_RUNNING = 3;
	
	public final static int PREDELIVERY_TEST_ERROR = 2;
	
	public final static int PREDELIVERY_TEST_EMAIL_NOT_SENT = 0;
	public final static int PREDELIVERY_TEST_EMAIL_SENT = 1;
	
	public final static int PREDELIVERY_TEST_FAILED = -2;
	
	/**
	 * Creates a predelivery test
	 * @param mailingID - mailing id 
	 * @param companyID - company id
	 * @param litmus_test_id - litmus test id (delivered from Litmus - SendEmailTest)
	 * @param test_email_address - email test to which the test email is to be sent 
	 *                             (delivered from Litmus - SendEmailTest)
	 * @return - true - success / false error
	 */
	public boolean createPredeliveryTest(int mailingID, @VelocityCheck int companyID,
			int litmus_test_id, String test_email_address);
	
	/**
	 * deletes a predelivery test based on a mailing id and a company id
	 * @param mailingID - mailing id
	 * @param companyID - company id
	 * @return
	 */
	public boolean deletePredeliveryTest(int mailingID, @VelocityCheck int companyID);

	/**
	 * Removes the all test results from an inbox test and marks the test as outdated.
	 * 
	 * @param mailingID mailing ID
	 * @param companyID company ID
	 */
	public void removeOutdatedPredeliveryTestData(int mailingID, @VelocityCheck int companyID);

	/**
	 * Checks, if the test data for given mailing is outdated.
	 * 
	 * @param mailingID mailing ID
	 * @param companyID company ID
	 * 
	 * @return <code>true</code> if test data is outdated
	 */
	public boolean isTestDataOutdated(final int mailingID, @VelocityCheck final int companyID);
	
	/**
	 * sets the flag for a predelivery test, that the test mail has been sent to Litmus
	 * @param mailingID - mailing id
	 * @param companyID - company id
	 * @return
	 */
	public boolean setTestMailHasBeenSent(int mailingID, @VelocityCheck int companyID);
	
	/**
	 * Updates a status for a test to finished
	 * ComPredeliveryDao.PRDELIVERY_TEST_COMPLETED 
	 * @param litmusTestId - id of the litmus test
	 * @return
	 */
	public boolean setStatusToFinishedForTest(int litmusTestId);
	
	/**
	 * Updates a status for a test to running (
	 * {@link ComPredeliveryDao#PREDELIVERY_TEST_RUNNING})
	 * @param litmusTestId - id of the litmus test
	 * @return
	 */
	public boolean setStatusToRunningForTest(int litmusTestId);
	
	/**
	 * Updates a status for a test to error
	 * ComPredeliveryDao.PREDELIVERY_TEST_ERROR
	 * @param litmusTestId id of the litmus 
	 * @param errrorMessage
	 * @return
	 */
	public boolean setStatusToErrorForTest(int litmusTestId, String errrorMessage);
	
	/**
	 * Creates a Predelivery test item.  There is one test item per provider (i.e. aol, hotmail, outlook..)
	 * @param testId - test id for which the predelivery item belongs to
	 * @param mailingID - mailing id
	 * @param companyID - company id
	 * @param provider - name of the provider.  i.e. aol, hotmail, outlook...
	 * @param imageTmbScNoImages - thumbnail image - email shortcut without images
	 * @param imageTmbSc - thumbnail image - email shortcut with images
	 * @param imageTmbFullNoImages - thumbnail image - full email without images
	 * @param imageTmbFull - thumbnail image - full email image
	 * @param imageScNoImages - image - email shortcut without images
	 * @param imageSc - image - email shortcut with images 
	 * @param imageFullNoImages image - full email without images
	 * @param imageFull - image -  full email image
	 * @param display Name - display name for test i.e. Outlook 2007 Windows/XP
	 * @return
	 * @throws Exception 
	 */
	public boolean createPredeliveryTestItem(int mailingID,
			@VelocityCheck int companyID, String provider,
			byte[] imageTmbScNoImages, byte[] imageTmbSc,
			byte[] imageTmbFullNoImages, byte[] imageTmbFull,
			byte[] imageScNoImages, byte[] imageSc, byte[] imageFullNoImages,
			byte[] imageFull, String displayName) throws Exception;
	
	/**
	 * 
	*  @param testId - test id for which the predelivery item spam belongs to
	 * @param mailingID - mailing id
	 * @param companyID - company id
	 * @param provider - name of the provider.  i.e. aol, hotmail, outlook...
	 * @param displayName -display name for test i.e. Outlook 2007 Windows/XP spam
	 * @return
	 */
	public boolean createPredeliveryTestItemSpam(int mailingID, @VelocityCheck int companyID, 
			String provider, String displayName, boolean isSpam);
	
	/**
	 * gets all test items for a test that have been completed
	 * @param testId
	 * @return - list of completedLitmusItems
	 */
	public List<CompleteLitmusItem> getAllCompletedTestItems(int mailingID,
			@VelocityCheck int companyID, boolean includeIncomplete);
	
	/**
	 * gets all test spam items for a test that have been completed
	 * @param testId
	 * @return - list of completedLitmusItems
	 */
	public List<CompleteLitmusItem> getAllCompletedTestItemsSpam(int mailingID,
			@VelocityCheck int companyID);
	
	/**
	 * gets the test status for a predelivery based on a mailing id and a company id
	 * will return -1, if no test exists
	 * will return -2, if an error has occured
	 * @param mailingID - mailing id
	 * @param companyID - company id
	 * @return
	 */
	public int getTestStatus(int mailingID, @VelocityCheck int companyID);
	
	/**
	 * Returns the error message for a test
	 * @param mailingID - mailing id
	 * @param companyID - company id
	 * @return
	 */
	public String getErrorMessage(int mailingID, @VelocityCheck int companyID);
	
	/**
	 * retrieves an image as a byte array based on the mailing id, company id, provider, and 
	 * image name
	 * @param mailingID - mailing id
	 * @param companyID - company id
	 * @param provider - provider, such as aol...
	 * @param imageName - name of image that should be retrieved
	 * @return
	 * @throws Exception 
	 */
	public byte[] getImage(int mailingID, @VelocityCheck int companyID, String provider, String imageName) throws Exception;
	
	/**
	 * returns a list of litmus polling items that should be still polling (tests that have not yet completed)
	 * @return
	 */
	public List<LitmusPollingItem> getAllLitmusThatShouldBePolling();
	
	/**
	 * returns the password for litmus
	 */	
	public String getLitmusPassword();
	
	/**
	 * returns the api-key for litmus
	 */
	public String getLitmusApiKey();
	
	/**
	 * returns the Litmus API-URL.
	 * @return
	 */
	public String getLitmusApiUrl();
	
	/**
	 * We need a different GUID-Prefix for each Licence we use.
	 * @return
	 */
	public String getLitmusGUIDPrefix();	
	
	public boolean deletePredeliveryByCompany(@VelocityCheck int companyID);
}
