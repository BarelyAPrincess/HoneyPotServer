package com.marchnetworks.management.instrumentation.dao;

import com.marchnetworks.command.common.dao.GenericHibernateDAO;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class DeletedDeviceDAOImpl extends GenericHibernateDAO<DeletedDevice, Long> implements DeletedDeviceDAO
{
	public DeletedDevice findByPathAndDevice( String path, DeviceMBean device )
	{
		Session session = ( Session ) getEntityManager().getDelegate();
		Criteria crit = session.createCriteria( entityType );

		crit.add( Restrictions.eq( "pathString", path ) );
		crit.add( Restrictions.eq( "address", device.getAddress() ) );
		crit.add( Restrictions.eq( "manufacturer", device.getManufacturer() ) );
		crit.add( Restrictions.eq( "manufacturerName", device.getManufacturerName() ) );
		crit.add( Restrictions.eq( "model", device.getModel() ) );
		crit.add( Restrictions.eq( "modelName", device.getModelName() ) );
		crit.add( Restrictions.eq( "macAddress", device.getMacAddress() ) );
		crit.add( Restrictions.eq( "serial", device.getSerial() ) );
		crit.add( Restrictions.eq( "name", device.getName() ) );
		crit.add( Restrictions.eq( "softwareVersion", device.getSoftwareVersion() ) );
		crit.add( Restrictions.eq( "hardwareVersion", device.getHardwareVersion() ) );
		crit.add( Restrictions.eq( "family", device.getFamily() ) );
		crit.add( Restrictions.eq( "familyName", device.getFamilyName() ) );

		DeletedDevice alert = ( DeletedDevice ) crit.uniqueResult();

		return alert;
	}
}

