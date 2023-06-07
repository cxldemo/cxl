package cn.com.agree.ide.abf.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.ide.IDE;

import cn.com.agree.commons.logging.Log4JLogFactory;
import cn.com.agree.ide.abf.entry.EndsModel;
import cn.com.agree.ide.abf.event.model.CheckCloseValue;
import cn.com.agree.ide.abf.event.model.CloseValue;
import cn.com.agree.ide.abf.event.model.EndValue;
import cn.com.agree.ide.abf.event.model.Event;
import cn.com.agree.ide.abf.event.model.FlowCloseValue;
import cn.com.agree.ide.abf.event.model.FlowEndValue;
import cn.com.agree.ide.abf.event.model.FlowValue;
import cn.com.agree.ide.abf.feature.FrameFeature;
import cn.com.agree.ide.abf.model.ComponentModel;
import cn.com.agree.ide.abf.model.EntryEventModel;
import cn.com.agree.ide.abf.model.EventArgsModel;
import cn.com.agree.ide.abf.model.EventModel;
import cn.com.agree.ide.abf.wizard.utils.EntryEventUtil;
import cn.com.agree.ide.authority.util.AuthorityUtil;
import cn.com.agree.ide.build.index.db.IndexTool;
import cn.com.agree.ide.commons.gef.entry.EntryKeys;
import cn.com.agree.ide.commons.gef.feature.IFeature;
import cn.com.agree.ide.commons.gef.model.ICommonModel;
import cn.com.agree.ide.commons.skeleton.ISkeletonConstants;
import cn.com.agree.ide.commons.skeleton.lfcutil.ILfcReuseHelper;
import cn.com.agree.ide.commons.skeleton.lfcutil.LfcCommonUtil;
import cn.com.agree.ide.commons.skeleton.utility.AdeInfoUtil;
import cn.com.agree.ide.commons.skeleton.utility.EditorMappingTools;
import cn.com.agree.ide.commons.skeleton.utility.EditorMappingsModel;
import cn.com.agree.ide.commons.skeleton.utility.MappingFileModel;
import cn.com.agree.ide.commons.skeleton.utility.ModuleMappingModel;
import cn.com.agree.ide.commons.skeleton.utility.ParaBean;
import cn.com.agree.ide.commons.skeleton.wizard.NewLfcWithAdeWizard;
import cn.com.agree.ide.utils.AStudioProjectUtil;
import cn.com.agree.ide.utils.AbideAuthKeys;
import cn.com.agree.ide.utils.PackageResourcePathTool;
import cn.com.agree.ide.utils.ResourceUtil;
import cn.com.agree.ide.utils.TextClearTool;
import cn.com.agree.ide.utils.UIStyleUtil;
import cn.com.agree.ide.utils.WorkspaceUtil;
import cn.com.agree.ide.utils.model.AstudioProjectModel;

/**
 * 
 * @author zhang.bj@cfischina.com
 *
 */
public class EventTypeChangeDialog extends EventDialog {

	private Object ends;
	private Text propName; // 事件名称 以及 事件指向的Flow地址
	private Text flowValue;
	private Group mappingGroup;
	private IWorkbenchWindow window;
	private Composite lfcContentContainer;
	private Composite aotuComposite;
	private Map<String, List<ParaBean>> lfcContent;
	private EditorMappingsModel mappings = new EditorMappingsModel();
	private List<ModuleMappingModel> moduleMappingModels = new ArrayList<>();
	private ICommonModel model;
	private static final String DATABASKET="DataBasket";
	private static Logger logger = Log4JLogFactory.getInstance(EventTypeChangeDialog.class);

	/**
	 * 
	 * @param model
	 *            当前model
	 * @param window
	 *            当前window
	 * @param event
	 *            将要保存的内容
	 * @param ends
	 *            是从FrameModel中获取的， 在调用的时候可以直接得到。所以直接传进来而非在这里再遍历出来 出口集合
	 * @param editFile
	 *            当前编辑器路径
	 */
	public EventTypeChangeDialog(IWorkbenchWindow window, ICommonModel model, Event event, Object ends, IFile editFile) {
		super(window.getShell(), model, event);
		this.window = window;
		this.ends = ends;
		this.model = model;
		this.editorIFile = editFile;
	}

	/**
	 * 为了实现当编辑快捷键事件时光标在事件名称框中
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Control parentControl = super.createDialogArea(parent);
		if (propName.getEnabled()) {
			propName.setFocus();
		}
		return parentControl;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.verticalSpacing = 15;
		container.setLayout(layout);
		GridData labelData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		/************************************** 设置事件名称 **************************************/
		Label propNameLabel = new Label(container, SWT.NONE);
		propNameLabel.setLayoutData(labelData);
		propNameLabel.setText("事件名称: ");
		propName = new Text(container, SWT.BORDER);
		propName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (clonedEvent.getName().startsWith(HOTKEY)) {
			/** 设置文本框接收快捷键 */
			EventTypeChangeTool.getKeySequenceText(propName);
			/** hot事件不显示前缀 */
			if (clonedEvent.getName().startsWith(HOTKEY)) {
				propName.setText(clonedEvent.getName().substring(HOTKEY.length(), clonedEvent.getName().length()));
				propName.setSelection(0, propName.getText().length());
			}
			propName.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					String keyName = propName.getText();
					String transform =	EventTypeChangeTool.getTypeTransform(keyName);
					if(transform != null){
						propName.setText(transform);
					}
					if (event.getName() == null || "".equals(event.getName())) {
						event.setName(HOTKEY + propName.getText());
					}
					clonedEvent.setName(HOTKEY + propName.getText());
					if (messageText != null) {
						messageText.setText("");
					}
				}
			});
		} else if (clonedEvent.getName().startsWith(AUTOEVENT)) {
			propName.setText(event.getName().substring(AUTOEVENT.length(), event.getName().length()));
			propName.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					clonedEvent.setName(propName.getText().trim());
				}
			});
		} else {
			propName.setEnabled(false);
			propName.setText(readAbfEventList(clonedEvent.getName()));
		}

		// type处理
		for (int i = 0; i < modelItems.size(); i++) {
			final TypeModel typeModel = modelItems.get(i);
			if (!clonedEvent.getName().equals("")) {
				if (i == 0) {
					clonedEvent.setType(typeModel.getName());
				}
			} else if (i == 0) {
				clonedEvent.setType(typeModel.getName());
			}
		}

		/**********************************************************************************/
		// 随机变动的面板
		aotuComposite = new Composite(container, SWT.NONE);
		GridLayout layoutChild = new GridLayout(2, false);
		layoutChild.marginWidth = 0;
		layoutChild.marginHeight = 0;
		aotuComposite.setLayout(layoutChild);
		GridData gdComposite = new GridData(GridData.FILL_BOTH);
		gdComposite.horizontalSpan = 2;
		aotuComposite.setLayoutData(gdComposite);
		updateContainer(aotuComposite);
	}

	/**
	 * 文件选择
	 */
	@Override
	protected void createControlSearch(Composite parent) {
		super.createControlSearch(parent);
		pattern.setText("*");
		pattern.addVerifyListener(new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				if (flowValue == null || flowValue.isDisposed()) {
					pattern.setText(null);
				}
			}
		});
		getList().addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent scEvent) {
				if (flowValue != null && (!flowValue.isDisposed())) {
					StructuredSelection selection = (StructuredSelection) scEvent.getSelection();
					if ((!selection.isEmpty())) {
						Object object = selection.getFirstElement();
						if (object != null && object instanceof IFile) {
							IFile file = (IFile) object;
							String choice = file.getFullPath().toString();
							if (flowValue.getText().equals(choice)) {
								return;
							}
							if (event.getType() != null && event.getType().equals(clonedEvent.getType())
									&& (event.getValue() != null && event.getValue().startsWith(choice))) {
								cloneEvent(event, clonedEvent);
							} else {
								clonedEvent.setValue(choice);
								clonedEvent.setEnds(new LinkedList<EndsModel>());
							}
							flowValue.setText(choice);
							// 检测存在性
							UIStyleUtil.checkTextPathExistAndMark(flowValue);
							flowValue.setToolTipText(choice);
							createLFCContent(aotuComposite, file);
						}
					}
				}
				if (messageText != null) {
					messageText.setText("");
				}
			}
		});
	}

	/**
	 * 配置面板长相
	 * 
	 * @param container
	 */
	private void updateContainer(Composite container) {
		Control[] children = container.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				children[i].dispose();
			}
		}
		switch (clonedEvent.getType()) {
		case END:
		case CLOSE:
		case CHECKCLOSE:
			updateContainerExit(container);
			break;
		case FLOW:
		case FLOWEND:
		case FLOWCLOSE:
			updateContainerFlow(container);
			break;
		default:
			// 默认
			Text temporary = new Text(container, SWT.NONE);
			GridData valueData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			valueData.heightHint = 22;
			temporary.setLayoutData(valueData);
			break;
		}
		container.layout();
	}

	/**
	 * 针对"出口"、"关闭出口"和"校验关闭"面板布局排版
	 * 
	 * @param container
	 */
	private void updateContainerExit(Composite container) {
		/**
		 * 结束 和 关闭
		 */
		Label closeLabel = new Label(container, SWT.NONE);
		closeLabel.setText("处理逻辑:");
		final Combo endsValue = new Combo(container, SWT.READ_ONLY);
		GridData valueData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		valueData.heightHint = 20;
		endsValue.setLayoutData(valueData);
		endsValue.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				EndsModel eModel = ((List<EndsModel>) ends).get(endsValue.getSelectionIndex());
				clonedEvent.setValue(eModel.getName());
			}
		});
		if (ends != null && ends instanceof List) {
			List<EndsModel> frameEnd = (List<EndsModel>) ends;
			List<String> items = new ArrayList<>();
			int index = 0;
			for (int i = 0; i < frameEnd.size(); i++) {
				EndsModel frameEndModel = frameEnd.get(i);
				if (!clonedEvent.getType().equals(CHECKCLOSE)||!"Back".equals(frameEndModel.getName())) {
					if (clonedEvent.getValue().equals("")) {
						clonedEvent.setValue(frameEndModel.getName());
					}
					items.add(frameEndModel.getCaption());
					if (frameEndModel.getName().equals(clonedEvent.getValue())) {
						index = i;
					}
				}
			}
			endsValue.setItems(items.toArray(new String[items.size()]));
			endsValue.select(index);
			initContent(container);
		}
	}

	/**
	 * 针对流程和流程关闭面板布局排版处理
	 * 
	 * @param container
	 */
	private void updateContainerFlow(final Composite container) {
		Link link = new Link(container, SWT.NONE);
		link.setText("<A>处理逻辑:</A>");
		linkFlow(link);
		
		// 定制文本框控件设置,包含一个文本和一个置于文本框右侧的清理label
		TextClearTool textClearTool = new TextClearTool(container);
		textClearTool.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// 获取文本框控件
		flowValue = textClearTool.getFilterText();
		flowValue.setText(clonedEvent.getValue());
		
		// 检测存在性
		UIStyleUtil.checkTextPathExistAndMark(flowValue);
		// 清理Label
		Label clean = textClearTool.getClearButton();
		lfcContentContainer = container;
		
		clean.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				//NOSONAR
			}

			@Override
			public void mouseDown(MouseEvent e) {
				clonedEvent.setType("");
				clonedEvent.setValue("");
				model.removeEntry(event.getName());
				Control[] children = container.getChildren();
				if (children != null) {
					for (int i = 0; i < children.length; i++) {
						Control control = children[i];
						if (control instanceof Group) {
							control.dispose();
						}
					}
				}
				initContent(container);
				change = true;
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				//NOSONAR
			}
		});

		if (!"".equals(clonedEvent.getValue())) {
			IFile lfcFile = ResourceUtil.getIFile(flowValue.getText());
			createLFCContent(container, lfcFile);
		} else {
			initContent(container);
		}
		container.layout();
	}

	/**
	 * 生成Flow类型时展现出入参和映射视图
	 * 
	 * @param container
	 * @param lfcFile
	 */
	private void createLFCContent(Composite container, IFile lfcFile) {
		if (null!=lfcFile&&lfcFile.exists()) {
			Control[] children = container.getChildren();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					Control control = children[i];
					if (control instanceof Group) {
						control.dispose();
					}
				}
			}
			/**
			 * 创建出入参数显示
			 */
			GridData paraGridData = new GridData(GridData.FILL_BOTH);
			paraGridData.heightHint = 180;
			paraGridData.horizontalSpan = 2;
			Group paraComposite = new Group(container, SWT.NONE);
			paraComposite.setLayout(new GridLayout(2, false));
			paraComposite.setLayoutData(paraGridData);
			/**
			 * 获取Flow事件中的出参、入参和映射值
			 */
			Object object = model.getValue(clonedEvent.getName());
			Map<String, String> inMap = new HashMap<>();
			Map<String, String> outMap = new HashMap<>();
			mappings.getMappingMap().clear();
			if (object instanceof FlowValue) {
				FlowValue flowValueLocal = (FlowValue) object;
				List<ParaBean> inParaBeans = flowValueLocal.getInPara();
				for (ParaBean paraBean : inParaBeans) {
					inMap.put(paraBean.getName(), paraBean.getValue());
				}
				List<ParaBean> outParaBeans = flowValueLocal.getOutPara();
				for (ParaBean paraBean : outParaBeans) {
					outMap.put(paraBean.getName(), paraBean.getValue());
				}
				mappings = EditorMappingsModel.clone(flowValueLocal.getMappings());
				moduleMappingModels = flowValueLocal.getModuleMappingModels();
			}
			String entryName = model.getFeature().getFeatureName();
			String eventName = clonedEvent.getName();
			lfcContent = LfcCommonUtil.getLfcPara(lfcFile, inMap, outMap);
			LfcCommonUtil.getInstance(new ILfcReuseHelper() {

				@Override
				public Collection<ParaBean> getLfcArgs(String type) {
					return lfcContent.get(type);
				}

				@Override
				public void beanChanged(ParaBean bean) {
					change = true;
				}
			}).createArgArea(paraComposite, LfcCommonUtil.FORCE_SHOW, LfcCommonUtil.FORCE_SHOW, entryName+"-"+eventName,SWT.HORIZONTAL);
			/**
			 * 创建映射展现视图
			 */
			mappingGroup = new Group(container, SWT.NONE);
			mappingGroup.setLayout(new GridLayout(2, false));
			mappingGroup.setLayoutData(paraGridData);
			mappingGroup.setText("数据映射");
			MappingFileModel mappingFileModel = new MappingFileModel(null, lfcFile, editorIFile);
			EditorMappingTools tools = new EditorMappingTools() {
				@Override
				public void configTableLayout(TableViewer viewer) {
					TableColumn[] column = viewer.getTable().getColumns();
					for (int i = 0; i < column.length; i++) {
						column[i].setWidth(80);
					}
				}
			};
			if (mappings.getMappingsPath() == null || mappings.getMappingsPath().equals("")) {
				mappingFileModel.setSourceFile(editorIFile);
				mappings.setMappingsPath(editorIFile.getFullPath().toPortableString());
				tools.createMappingTable(mappingGroup, mappings, moduleMappingModels, mappingFileModel, EditorMappingTools.STYLE_ONE, true);
			} else {
				mappingFileModel.setSourceFile(ResourceUtil.getIFile(mappings.getMappingsPath()));
				tools.createMappingTable(mappingGroup, mappings, moduleMappingModels, mappingFileModel, EditorMappingTools.STYLE_ONE, false);
			}
			tools.getMappingComposite().layout();
		}
		container.layout();
	}

	/**
	 * 在选择不是流程编辑的时候默认都不能编辑
	 * 
	 * @param container
	 */
	private void initContent(Composite container) {

		GridData paraGridData = new GridData(GridData.FILL_BOTH);
		paraGridData.heightHint = 180;
		paraGridData.horizontalSpan = 2;
		/**
		 * 获取Flow事件中的出参、入参和映射值
		 */
		Group para = new Group(container, SWT.NONE);
		para.setLayout(new FillLayout());
		para.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
		para.setText("出入参");

		/**
		 * 创建映射展现视图
		 */
		mappingGroup = new Group(container, SWT.NONE);
		mappingGroup.setLayoutData(paraGridData);
		mappingGroup.setText("数据映射");

		container.layout();
	}

	/**
	 * 如果指向的LFC文件存在就执行调整， 不存在执行创建
	 * 
	 * @param link
	 */
	private void linkFlow(Link link) {
		Listener[] listeners = link.getListeners(SWT.Selection);
		if (listeners.length != 0) {
			return;
		}
		link.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = flowValue.getText().trim();
				if (value != null && !value.equals("")) {
					IFile file = ResourceUtil.getIFile(value);
					if (!file.getProject().exists() || !file.getProject().isOpen()) {
						MessageBox mb = new MessageBox(window.getShell(), SWT.ICON_WARNING | SWT.OK);
						mb.setText("No longer exists");
						mb.setMessage("错误路径!");
						mb.open();
						flowValue.setText("");
						return;
					}
					if (file.exists()) {
						try {
							IDE.openEditor(window.getActivePage(), file, true);
						} catch (PartInitException e1) {
							logger.error("PartInitException",e1);
						}
						okPressed();
					}
				} else {
					//创建lfc前，判断是否有权限
					if(!AuthorityUtil.hasAuth(AbideAuthKeys.CREATE_LFC)) {
						return;
					}
					String modelID = "";
					if (model.getValue(ID) != null) {
						modelID = model.getValue(ID).toString();
					} else if (model.getFeature() instanceof FrameFeature) {
						modelID = editorIFile.getFullPath().removeFileExtension().lastSegment();
					}
					String eventName = clonedEvent.getName();
					String abfName = editorIFile.getName().substring(0, editorIFile.getName().indexOf('.'));
					if(eventName.startsWith("HotKey#")) {
						eventName = eventName.replace("HotKey#", "");
					}
					if(eventName.contains("+")) {
						eventName = eventName.replaceAll("\\+", "_");
					}
					NewLfcWithAdeWizard wizard = new NewLfcWithAdeWizard(abfName + "_" + modelID + "_" + eventName);
					wizard.init(window.getWorkbench(), new StructuredSelection(editorIFile.getParent()));
					//获取abf4a的数据字典，存放到Wizard中
					ICommonModel frameModel = getFrameModel(model);
					Object object = frameModel.getValue(AdeInfoUtil.DATABASKET);
					Object mesObject = frameModel.getValue(EntryKeys.MESSAGES);
					if(object != null) {
						wizard.setAdes((List)object);
					}
					if(mesObject != null) {
						wizard.setMessages((List)mesObject);
					}
					WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
					int windowLocal = dialog.open();
					if (windowLocal == Window.CANCEL) {
						return;
					}
					String newFlowFilePath = wizard.getFilePath();
					IFile newFlowFile = wizard.getFile();
					if (newFlowFile == null) {
						newFlowFile = ResourceUtil.getIFile(newFlowFilePath);
					}
					// 检测存在性
					UIStyleUtil.checkTextPathExistAndMark(flowValue);
					flowValue.setText(newFlowFilePath);
					clonedEvent.setValue(newFlowFilePath);
					createLFCContent(lfcContentContainer, newFlowFile);
					okPressed();
				}
			}
		});
	}
	
	/**
	 * 获取外框
	 */
	public ICommonModel getFrameModel(ICommonModel commonModel) {
		IFeature parentFeature = commonModel.getFeature();
		if(parentFeature instanceof FrameFeature) {
			return commonModel;
		}else {
			ICommonModel parentModel = commonModel.getParent();
			if(parentModel == null) {
				return null;
			}
			return getFrameModel(parentModel);
		}
	}

	/**
	 * 保存
	 * 
	 */
	@Override
	protected boolean resultOkPressed() {
		switch (clonedEvent.getType()) {
		case END:
			boolean endChange = eventEndChange();
			if (endChange) {
				model.setValue(clonedEvent.getName(), new EndValue(clonedEvent.getValue()));
				cloneEvent(clonedEvent, event);
				change = true;
			}
			break;
		case CLOSE:
			boolean closeChange = eventEndChange();
			if (closeChange) {
				model.setValue(clonedEvent.getName(), new CloseValue(clonedEvent.getValue()));
				cloneEvent(clonedEvent, event);
				change = true;
			}
			break;
		case CHECKCLOSE:
			boolean checkcloseChange = eventEndChange();
			if (checkcloseChange) {
				model.setValue(clonedEvent.getName(), new CheckCloseValue(clonedEvent.getValue()));
				cloneEvent(clonedEvent, event);
				change = true;
			}
			break;
		case FLOW:
		case FLOWEND:
		case FLOWCLOSE:

			/**
			 * 判断内容是否有过改变
			 */
			boolean flowChange = eventEndChange();
			if (flowChange) {
				change = true;
			}
			Object oldMapping = model.getValue(clonedEvent.getName());
			EditorMappingsModel oldMappings = null;
			if (oldMapping != null&&oldMapping instanceof FlowValue) {
				FlowValue flowValueLocal = (FlowValue) oldMapping;
				if(!flowValueLocal.getModuleMappingModels().isEmpty()) {
					change = true;
				}
				oldMappings = EditorMappingsModel.clone(flowValueLocal.getMappings());
			}
			boolean mappingChange = false;
			if (oldMappings != null) {
				if(oldMappings.getMappingsPath() != mappings.getMappingsPath()){
					mappingChange = true;
				} else {
					mappingChange = EditorMappingTools.isMappingChanged(oldMappings.getMappingMap(),
							mappings.getMappingMap());
				}
			}
			
			if (mappingChange) {
				change = true;
			}
			/**
			 * 对改动的进行处理
			 */
			if (change) {
				FlowValue flowModel = null;
				event.setType(clonedEvent.getType());
				if (clonedEvent.getType().equals(FLOWEND) || clonedEvent.getType().equals(FLOWCLOSE)) {
					if (clonedEvent.getType().equals(FLOWEND)) {
						flowModel = new FlowEndValue();
					} else if (clonedEvent.getType().equals(FLOWCLOSE)) {
						flowModel = new FlowCloseValue();
					}
					StringBuilder sb = new StringBuilder(clonedEvent.getValue());
					List<EndsModel> chooseEnds = clonedEvent.getEnds();
					for (int i = 0; i < chooseEnds.size(); i++) {
						sb.append(", ");
						sb.append(chooseEnds.get(i));
					}
					if(flowModel != null){
						flowModel.setValue(sb.toString());
					}
					event.setValue(sb.toString());
				} else if (clonedEvent.getType().equals(FLOW)) {
					flowModel = new FlowValue();
					flowModel.setValue(clonedEvent.getValue());
					event.setValue(clonedEvent.getValue());
					flowModel.setUri(PackageResourcePathTool.processFullPath(clonedEvent.getValue(), editorIFile));
				}
				// 出入参
				if (flowModel != null && lfcContent != null) {
					flowModel.setInPara(lfcContent.get(ISkeletonConstants.IN_ARGS));
					flowModel.setOutPara(lfcContent.get(ISkeletonConstants.OUT_ARGS));
				}
				// mapping
				if (flowModel != null){
					flowModel.setMappings(mappings);
					flowModel.setModuleMappingModels(moduleMappingModels);
				}
				
				// 对hotkey进行特殊处理
				if (clonedEvent.getName().startsWith(HOTKEY)) {
					model.removeEntry(event.getName());
					cloneEvent(clonedEvent, event);
				}
				// 对自定义事件进行特殊处理
				if (event.getName().startsWith(AUTOEVENT)) {
					event.setName(event.getName().substring(AUTOEVENT.length(), event.getName().length()));
					if(clonedEvent.getName().startsWith(AUTOEVENT)){
						clonedEvent.setName(clonedEvent.getName().substring(AUTOEVENT.length(), clonedEvent.getName().length()));
					}
					if(!(clonedEvent.getName().equals(event.getName()))){
						model.removeEntry(event.getName());
						event.setName(clonedEvent.getName());
					}
				}
				model.setValue(clonedEvent.getName(), flowModel);
			} else {
				// 对自定义事件进行特殊处理
				if (event.getName().startsWith(AUTOEVENT)) {
					event.setName(event.getName().substring(AUTOEVENT.length(), event.getName().length()));
				}
			}
			addDataBasketForLFC();
			break;
		}
		return true;
	}
	
	public Event getEvent() {
		return clonedEvent;
	}

	/**
	 * 将lfc中的数据字典存入当前abf4a中
	 */
	private void addDataBasketForLFC() {
		String eventPath = clonedEvent.getValue();
		Set<String> ades = IndexTool.getDataBasketADES(ResourceUtil.getIFile(eventPath));
		ICommonModel fmodel = getFrameModel(model);
		Object obj = fmodel.getValue(DATABASKET);
		if(obj == null || ((List) obj).isEmpty()) {
			fmodel.setValue(DATABASKET, AdeInfoUtil.getAdeItemCollForColl(ades));
		} else {
			ades.addAll(AdeInfoUtil.getCollForAdeItemColl(((List) obj)));
			fmodel.setValue(DATABASKET, AdeInfoUtil.getAdeItemCollForColl(ades));
		}
	}
	
	/**
	 * 判断内容是否有过变动
	 * 
	 * @return
	 */
	private boolean eventEndChange() {
		if (clonedEvent.getName().equals(event.getName())&&clonedEvent.getType().equals(event.getType())) {
			switch (clonedEvent.getType()) {
			case FLOW:
			case FLOWEND:
			case FLOWCLOSE:
				String[] split = event.getValue().split(",");
				if (clonedEvent.getValue().equals(split[0])) {
					return false;
				}
				break;
			default:
				if (clonedEvent.getValue().equals(event.getValue())) {
					return false;
				}
				break;
			}
		}
		return true;
	}

	protected boolean getChange() {
		return change;
	}
	
	@Override
	protected ItemsFilter createFilter() {
		List<IContainer> searchContainer = new ArrayList<>();
		searchContainer.add(editorIFile.getParent());//当前文件的目录
		List<IResource> resources = WorkspaceUtil.getBusinessProjects();
		for(int i = 0; i < resources.size(); i++) {
			searchContainer.add((IContainer) resources.get(i));
		}
		Map<String, AstudioProjectModel> astudioMap = AStudioProjectUtil.getInstance().getAstudioProjectModels();
		for(AstudioProjectModel astudioProjectModel : astudioMap.values()) {
			List<Object> iContainers = astudioProjectModel.getResourceLFCList();
			for(int i = 0; i < iContainers.size(); i++) {
				searchContainer.add((IContainer) iContainers.get(i));//公共LFC目录
			}
		}
		return new EventResourceFilter(searchContainer.toArray(new IContainer[0]), isDerived(), getTypeMask());
	}
	
	protected class EventResourceFilter extends ResourceFilter{
		
		private List<SearchPattern> containersPattern = new ArrayList<>();
		
		public EventResourceFilter(IContainer[] searchContainer, boolean showDerived, int typeMask) {
			super(searchContainer[0], showDerived, typeMask);
			//如果输入"*"，仅对当前文件目录和公共lfc目录中资源进行匹配
			if(getPattern().equals("**")) {
				for(int i=0;i<searchContainer.length;i++) {
					SearchPattern searchPattern = new SearchPattern(SearchPattern.RULE_EXACT_MATCH
							| SearchPattern.RULE_PREFIX_MATCH | SearchPattern.RULE_PATTERN_MATCH);
					if(searchContainer[i] != null) {
						searchPattern.setPattern(searchContainer[i].getFullPath().toString());
						containersPattern.add(searchPattern);
					}
				}
			}
		}
		
		@Override
		protected boolean matchName(IResource resource) {
			String name = resource.getName();
			if(name.startsWith("."))
				return false;
			int index = name.lastIndexOf('.');
			name = index == -1 ? name : name.substring(0, index);
			if (nameMatches(name)) {
				if (!containersPattern.isEmpty()) {
					// match full container path:
					String containerPath = resource.getParent().getFullPath().toString();
					for (SearchPattern searchPattern : containersPattern) {
						boolean b = searchPattern.matches(containerPath);
						if(b) {
							return true;
						}
					
					}
					return false;
				}else if (getContainerPattern() != null) {
					// match full container path:
					String containerPath = resource.getParent().getFullPath().toString();
					if (getContainerPattern().matches(containerPath)) {
						return true;
					}
					// match path relative to current selection:
					if (getRelativeContainerPattern() != null) {
						return getRelativeContainerPattern().matches(containerPath);
					}
					return false;
				}
				
				// 当前文件下的即时是私有也显示
				if (resource.getParent().equals(editorIFile.getParent())) {
					IContainer folder = editorIFile.getParent();
					try {
						for (IResource res : folder.members()) {
							if (res.getFullPath().toString().equals(resource.getFullPath().toString())) {
								return true;
							}
						}
					} catch (CoreException e) {
						logger.error(e.getMessage());
					}
				} else {
					String flag = IndexTool.getAccessibilityForFile((IFile)resource);
					return !"private".equals(flag);
				}
				return true;
			}
			return false;
		}
		
		@Override
		protected boolean matchCaption(IFile file) {
			if(!containersPattern.isEmpty())
				return false;
			return super.matchCaption(file);
		}
	}

	/**
	 * 
	 * 根据abf的组件及事件确定显示名称
	 * 
	 */
	private String readAbfEventList(String eventName) {
		StringBuilder eventArg = new StringBuilder();
		EntryEventModel entryEventModel = EntryEventUtil.getInstance().getEventArgs();
		List<ComponentModel> componentModels = entryEventModel.getComponent();
		for(ComponentModel componentModel : componentModels) {
			// 组件名称匹配
			String entryName = model.getFeature().getFeatureName();
			if(entryName.equals(componentModel.getName())) {
				for(EventModel eventModel : componentModel.getEvent()) {
					if(eventName.equals(eventModel.getEventName())) {
						for(EventArgsModel eventArgsModel : eventModel.geteArgsModels()) {
							eventArg.append(eventArgsModel.getArgName() + ",");
						}
					}
				}
			}
			
		}
		if(!"".equals(eventArg.toString())){
			return eventName + "("+ eventArg.toString().substring(0, eventArg.toString().length() - 1) +")";
		}
		return eventName;
	}
}
