
package au.gov.sbr.core.forms;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class instantiates SBR Form objects.
 * 
 * @author SBR
 */
public class FormFactory {

	private static Logger logger = Logger
			.getLogger(FormFactory.class.getName());

	private static FormFactory instance = new FormFactory();

	/**
	 * Constructs and returns a FormFactory instance object.
	 * 
	 * @return the FormFactory instance.
	 */
	public static FormFactory instance() {
		return instance;
	}

	private List<Form> formList;

	private FormFactory() {
		List<Form> formList = new ArrayList<Form>();

		String formPath = '/' + FormFactory.class.getPackage().getName()
				.replace('.', '/');

		InputStream stream = getClass().getClassLoader().getResourceAsStream(
				FormFactory.class.getName().replace('.', '/') + ".properties");
		Properties properties = new Properties();

		try {
			properties.load(stream);
			for (String property : properties.stringPropertyNames()) {
				String formName = properties.getProperty(property);
				Properties formProperties = new Properties();
				formProperties.load(getClass().getResourceAsStream(
						formPath + '/' + formName));

				formList.add(new Form(formProperties));
			}

			Collections.sort(formList, new Comparator<Form>() {
				@Override
				public int compare(Form form1, Form form2) {
					return form1.getName().compareTo(form2.getName());
				}
			});

			this.formList = Collections.unmodifiableList(formList);
		} catch (IOException ex) {
			this.formList = Collections.emptyList();
			logger.log(Level.SEVERE, "Unable to create form list", ex);
		}
	}

	/**
	 * Returns a list of Form objects.
	 * 
	 * @return the list of Forms.
	 */
	public List<Form> getFormList() {
		return formList;
	}
}
