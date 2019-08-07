-- SET DEFINE OFF;
UPDATE messages_tbl SET value_default = 'OpenEMM', value_de = 'OpenEMM', value_es = 'OpenEMM', value_fr = 'OpenEMM', value_it = 'OpenEMM', value_nl = 'OpenEMM', value_pt = 'OpenEMM' WHERE message_key = 'default.EMM';
UPDATE messages_tbl SET
	value_default = 'This login protects confidential and privileged information. Any unauthorized use of this access and copying, disclosure or distribution of its data is strictly forbidden.',
	value_de = 'Dieser Zugang schützt vertrauliche und rechtlich geschützte Informationen. Die unautorisierte Nutzung dieses Zugangs und das Kopieren, Veröffentlichen oder Weitergeben seiner Daten ist nicht gestattet.',
	value_es = 'Este acceso protege informaciones confidenciales y legales. El empleo no autorizado de este acceso y la reproducción, publicación y transferencia de sus datos no están permitidos.',
	value_fr = 'Cet accès protège les informations confidentielles et protégées par la loi. L''utilisation non autorisée de cet accès et la copie, la publication ou la distribution des données ainsi obtenues est strictement interdite.',
	value_it = 'Questo accesso protegge le informazioni riservate e protette. L''''utilizzo non autorizzato di questo accesso e la copia, la pubblicazione o la trasmissione dei relativi dati non sono consentiti.',
	value_nl = 'Deze toegang beschermt vertrouwelijke en beschermde informatie. Ongeautoriseerd gebruik van deze toegang en het kopiëren, bekendmaken of doorgeven van daarmee samenhangende gegevens is strikt verboden.',
	value_pt = NULL
	WHERE message_key = 'logon.security';
