package spirit.fitness.scanner.util;

import spirit.fitness.scanner.receving.ItemsPannel;
import spirit.fitness.scanner.report.DailyReport;
import spirit.fitness.scanner.report.ModelZone2Report;
import spirit.fitness.scanner.search.QueryPannel;
import spirit.fitness.scanner.shipping.ShippingConfirm;
import spirit.fitness.scanner.shipping.ShippingPicking;

public class InstanceUtil {

	public static boolean isExits() 
	{
		if(ItemsPannel.isExit() || QueryPannel.isExit() || ShippingConfirm.isExit() || DailyReport.isExit() || ShippingPicking.isExit() || ModelZone2Report.isExit())
			return true;
		return false;
	}
}
