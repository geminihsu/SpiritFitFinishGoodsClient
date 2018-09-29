package spirit.fitness.scanner.util;

import spirit.fitness.scanner.AppMenu;
import spirit.fitness.scanner.delegate.ItemPanelBaseViewDelegate;
import spirit.fitness.scanner.receving.ContainerPanel;

import spirit.fitness.scanner.report.DailyInventoryReport;
import spirit.fitness.scanner.report.DailyShippingReport;
import spirit.fitness.scanner.report.ReplenimentReport;
import spirit.fitness.scanner.search.QueryPanel;
import spirit.fitness.scanner.shipping.ShippingConfirm;
import spirit.fitness.scanner.shipping.ShippingPicking;

public class InstanceUtil {

	public static boolean isExits() 
	{
	
		if(AppMenu.isExistInstance || ItemPanelBaseViewDelegate.isExit() || QueryPanel.isExit() || ContainerPanel.isExit() || ShippingConfirm.isExit() || DailyInventoryReport.isExit() || ShippingPicking.isExit() || ReplenimentReport.isExit()||DailyShippingReport.isExit())
			return true;
		return false;
	}
}
