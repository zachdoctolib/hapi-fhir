package ca.uhn.fhir.jpa.provider.r4;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.uhn.fhir.jpa.interceptor.CascadingDeleteInterceptor;
import ca.uhn.fhir.rest.api.DeleteCascadeModeEnum;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResourceProviderCascadeDeleteR4Test extends BaseResourceProviderR4Test {
	String idPartReport = "R1";
	IdType reportId = new IdType("DiagnosticReport/" + idPartReport);
	IdType obs1Id = new IdType("Observation/O1");

	@BeforeEach
	private void addCascadingDeleteInterceptor() {
		ourRestServer.getInterceptorService().registerInterceptor(new CascadingDeleteInterceptor(getContext(), myDaoRegistry, myInterceptorRegistry));

		Observation o1 = new Observation();
		o1.setId(obs1Id);
		o1.setStatus(Observation.ObservationStatus.FINAL);

		DiagnosticReport r = new DiagnosticReport();
		r.setId(reportId);
		r.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
		r.addResult(new Reference(o1));

		myObservationDao.update(o1);
		myDiagnosticReportDao.update(r);
	}

	@Test
	public void testCascadeDeleteDiagnosticReportObservations() {

		myClient.delete().resourceById(reportId).cascade(DeleteCascadeModeEnum.DELETE).execute();

		// all resources should be gone now
		assertThrows(ResourceGoneException.class, () -> myDiagnosticReportDao.read(reportId), "Report was not deleted");
		assertThrows(ResourceGoneException.class, () -> myObservationDao.read(obs1Id), "referenced Observation was not deleted");
	}

	@Test
	public void testCascadeDeleteDiagnosticReportObservationsConditional() {

		myClient.delete().resourceConditionalByUrl("DiagnosticReport?_id=" + idPartReport)
			.cascade(DeleteCascadeModeEnum.DELETE).execute();

		// all resources should be gone now
		assertThrows(ResourceGoneException.class, () -> myDiagnosticReportDao.read(reportId), "Report was not deleted");
		assertThrows(ResourceGoneException.class, () -> myObservationDao.read(obs1Id), "referenced Observation was not deleted");
	}
}
